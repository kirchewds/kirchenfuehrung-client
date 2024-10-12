package de.kirchewds.kirchenfuehrung.client.playback

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.mp3.Mp3Extractor
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import de.kirchewds.kirchenfuehrung.client.R
import de.kirchewds.kirchenfuehrung.client.data.ToursRepository
import de.kirchewds.kirchenfuehrung.client.model.Tour
import de.kirchewds.kirchenfuehrung.client.model.Track
import de.kirchewds.kirchenfuehrung.client.ui.MainActivity
import de.kirchewds.kirchenfuehrung.client.util.CoilBitmapLoader
import de.kirchewds.kirchenfuehrung.client.util.metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.plus
import okhttp3.OkHttpClient
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class MediaPlaybackService: MediaLibraryService(), WithDataSources, Player.Listener, MediaLibraryService.MediaLibrarySession.Callback {
    @Inject override lateinit var toursRepository: ToursRepository
    @Inject override lateinit var playerCache: SimpleCache
    @Inject override lateinit var httpClient: OkHttpClient
    override val context = this

    private val scope = CoroutineScope(Dispatchers.IO) + Job()
    lateinit var player: ExoPlayer
    private lateinit var bitmapLoader: CoilBitmapLoader
    private lateinit var mediaSession: MediaLibrarySession
    private var binder = MusicBinder()
    private lateinit var audioManager: AudioManager

    val currentMediaMetadata = MutableStateFlow<Track?>(null)

    private val headsetStateReceiver = object: BroadcastReceiver() {
        val supportedActions = arrayOf(
            Intent.ACTION_HEADSET_PLUG,
            "android.bluetooth.headset.action.STATE_CHANGED",
            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED"
        )

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action in supportedActions) updateDevices()
        }
    }

    private lateinit var cacheDataSource: CacheDataSource.Factory
    private lateinit var dataSource: DataSource.Factory
    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(
                this,
                { NOTIFICATION_ID },
                CHANNEL_ID,
                R.string.music_player
            )
                .apply {
                    setSmallIcon(R.drawable.ic_launcher_foreground)
                }
        )
        cacheDataSource = createCacheDataSource(false)
        dataSource = cacheDataSource
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource, Mp3Extractor.FACTORY))
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(), true
            )
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()
            .apply {
                addListener(this@MediaPlaybackService)
                repeatMode = Player.REPEAT_MODE_OFF
            }
        bitmapLoader = CoilBitmapLoader(this, scope)
        mediaSession = MediaLibrarySession.Builder(this, player, this)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setBitmapLoader(bitmapLoader)
            .build()

        audioManager = applicationContext.getSystemService()!!
        registerReceiver(headsetStateReceiver, IntentFilter().apply {
            headsetStateReceiver.supportedActions.forEach { addAction(it) }
        }, RECEIVER_NOT_EXPORTED)
    }

    private val supportedTypes by lazy {
        val list = HashMap<Int, Int>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        list[AudioDeviceInfo.TYPE_BLE_HEADSET] = 2
                    }
                    list[AudioDeviceInfo.TYPE_HEARING_AID] = 2
                }
                list[AudioDeviceInfo.TYPE_USB_HEADSET] = 3
            }
            list[AudioDeviceInfo.TYPE_AUX_LINE] = 1
            list[AudioDeviceInfo.TYPE_WIRED_HEADSET] = 3
            list[AudioDeviceInfo.TYPE_WIRED_HEADPHONES] = 3
            // For bluetooth audio - probably not a box
            list[AudioDeviceInfo.TYPE_BLUETOOTH_A2DP] = 1
            list[AudioDeviceInfo.TYPE_BLUETOOTH_SCO] = 1
        }
        list
    }

    private val supportedDevices @RequiresApi(Build.VERSION_CODES.M)
    get() = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).filter { it.type in supportedTypes.keys }

    private fun updateDevices() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (supportedDevices.isNotEmpty()) {
            player.setPreferredAudioDevice(
                this.supportedDevices
                    .groupBy { supportedTypes[it.type] }
                    .maxBy { it.key!! }
                    .value[0]
            )
        }
    }

    val isUsingHeadphones: Boolean get() =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) audioManager.isWiredHeadsetOn
        else {
            updateDevices()
            supportedDevices.isNotEmpty()
        }

    fun play(item: Tour) {
        player.clearMediaItems()
        player.setMediaItems(item.tracks.map(Track::toMediaItem))
        player.prepare()
        preload(item.tracks)
    }

    private fun preload(tracks: List<Track>) {
        for (track in tracks) {
            track.image?.let(bitmapLoader::preload)
            val downloadRequest = DownloadRequest.Builder(track.id, track.audio).setCustomCacheKey(track.id).build()
            DownloadService.sendAddDownload(context, MediaDownloadService::class.java, downloadRequest, false)
        }
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    override fun onDestroy() {
        mediaSession.release()
        player.removeListener(this)
        player.release()
        playerCache.release()
        unregisterReceiver(headsetStateReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder = super.onBind(intent) ?: binder

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(
        LibraryResult.ofItem(
            MediaItem.Builder()
                .setMediaId(ROOT)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsPlayable(false)
                        .setIsBrowsable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .build()
                )
                .build(),
            params
        )
    )

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) player.pause()
        currentMediaMetadata.value = mediaItem?.metadata
        super.onMediaItemTransition(mediaItem, reason)
    }

    inner class MusicBinder: Binder() {
        val service: MediaPlaybackService get() = this@MediaPlaybackService
    }

    companion object {
        const val ROOT = "root"
        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888
    }
}