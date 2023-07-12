package io.gitlab.jfronny.kirchenfuehrung.client.playback

import android.app.PendingIntent
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Binder
import android.os.IBinder
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mp3.Mp3Extractor
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import io.gitlab.jfronny.kirchenfuehrung.client.R
import io.gitlab.jfronny.kirchenfuehrung.client.data.ToursRepository
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.model.Track
import io.gitlab.jfronny.kirchenfuehrung.client.ui.MainActivity
import io.gitlab.jfronny.kirchenfuehrung.client.util.CoilBitmapLoader
import io.gitlab.jfronny.kirchenfuehrung.client.util.collect
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.inject.Inject
import androidx.annotation.OptIn as OptInX

@OptInX(UnstableApi::class)
@AndroidEntryPoint
class MediaPlaybackService: MediaLibraryService(), Player.Listener, MediaLibraryService.MediaLibrarySession.Callback {
    @Inject lateinit var toursRepository: ToursRepository
    @Inject lateinit var playerCache: SimpleCache

    private val scope = CoroutineScope(Dispatchers.Main) + Job()
    private lateinit var connectivityManager: ConnectivityManager
    lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession
    private var binder = MusicBinder()

    private var currentTour: Tour? = null

    val currentMediaMetadata = MutableStateFlow<Track?>(null)
    private var currentTrack: Track? = null

    @OptInX(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(this, {NOTIFICATION_ID}, CHANNEL_ID, R.string.music_player)
                .apply {
                    setSmallIcon(R.drawable.ic_client_placeholder) //TODO nice small icon
                }
        )
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(createMediaSourceFactory())
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(), true
            )
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()
            .apply {
                addListener(this@MediaPlaybackService)
                repeatMode = Player.REPEAT_MODE_OFF
            }
        mediaSession = MediaLibrarySession.Builder(this, player, this)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setBitmapLoader(CoilBitmapLoader(this, scope))
            .build()
        connectivityManager = getSystemService()!!

        currentMediaMetadata.collect(scope) {
            currentTrack = it
        }
    }

    private fun createOkHttpDataSourceFactory() =
        OkHttpDataSource.Factory(OkHttpClient.Builder().build())

    @OptInX(UnstableApi::class)
    private fun createCacheDataSource(): CacheDataSource.Factory =
        CacheDataSource.Factory()
        .setCache(playerCache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(this, createOkHttpDataSourceFactory()))

    private fun createDataSourceFactory(): DataSource.Factory {
        val trackUrlCache = HashMap<String, Url>()
        return ResolvingDataSource.Factory(createCacheDataSource()) { dataSpec ->
            val mediaId = dataSpec.key ?: error("No media id")

            trackUrlCache[mediaId]?.let {
                return@Factory dataSpec.withUri(it.toString().toUri())
            }

            val track = runBlocking(Dispatchers.IO) {
                toursRepository.getTrack(mediaId)
            }.getOrElse {
                when (it) {
                    is NullPointerException -> throw PlaybackException(getString(R.string.error_not_found), it, PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND)
                    is ConnectException, is UnknownHostException -> throw PlaybackException(getString(R.string.error_no_internet), it, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
                    is SocketException -> throw PlaybackException(getString(R.string.error_timeout), it, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT)
                    else -> throw PlaybackException(getString(R.string.error_unknown), it, PlaybackException.ERROR_CODE_REMOTE_ERROR)
                }
            }
            trackUrlCache[mediaId] = track.audio

            dataSpec.withUri(track.audio.toString().toUri()).subrange(dataSpec.uriPositionOffset, CHUNK_LENGTH)
        }
    }

    private fun createExtractorsFactory() = ExtractorsFactory {
        arrayOf(Mp3Extractor())
    }

    private fun createMediaSourceFactory(): MediaSource.Factory = DefaultMediaSourceFactory(createDataSourceFactory(), createExtractorsFactory())

    fun play(item: Tour) {
        player.clearMediaItems()
        player.setMediaItems(item.tracks.map { it.toMediaItem() })
        player.prepare()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    @OptInX(UnstableApi::class)
    override fun onDestroy() {
        mediaSession.release()
        player.removeListener(this)
        player.release()
        playerCache.release()
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
    }

    inner class MusicBinder: Binder() {
        val service: MediaPlaybackService get() = this@MediaPlaybackService
    }

    companion object {
        const val ROOT = "root"
        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888
        const val CHUNK_LENGTH = 512 * 1024L
    }
}