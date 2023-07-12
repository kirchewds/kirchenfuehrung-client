package io.gitlab.jfronny.kirchenfuehrung.client.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import io.gitlab.jfronny.kirchenfuehrung.client.data.ToursRepository
import io.gitlab.jfronny.kirchenfuehrung.client.playback.MediaPlaybackService
import io.gitlab.jfronny.kirchenfuehrung.client.playback.PlayerConnection
import io.gitlab.jfronny.kirchenfuehrung.client.ui.viewer.LocalPlayerConnection
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repository: ToursRepository

    private var playerConnection by mutableStateOf<PlayerConnection?>(null)
    private var mediaController: MediaController? = null
    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            if (p1 is MediaPlaybackService.MusicBinder) {
                playerConnection = PlayerConnection(p1, lifecycleScope)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            playerConnection?.dispose()
            playerConnection = null
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MediaPlaybackService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController?.release()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            { mediaController = controllerFuture.get() },
            MoreExecutors.directExecutor()
        )

        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            CompositionLocalProvider(
                LocalPlayerConnection provides playerConnection
            ) {
                ClientApp(repository, widthSizeClass)
            }
        }
    }
}
