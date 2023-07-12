package io.gitlab.jfronny.kirchenfuehrung.client.ui.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import io.gitlab.jfronny.kirchenfuehrung.client.ui.ClientNavigationActions
import kotlinx.coroutines.runBlocking

@Composable
fun ViewerRoute(
    viewerViewModel: ViewerViewModel,
    isExpandedScreen: Boolean,
    navigation: ClientNavigationActions,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val currentTrack by playerConnection.currentTrack.collectAsState(initial = null)

    Scaffold(
        modifier = Modifier
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            Column {
                if (currentTrack != null) {
                    AsyncImage(
                        model = currentTrack!!.image,
                        contentDescription = null
                    )
                    Text(text = currentTrack!!.name)
                }
                Text(text = viewerViewModel.getTourId())
                Button(onClick = {
                    //TODO prettier with loading screen
                    runBlocking {
                        playerConnection.play(viewerViewModel.getTour().getOrThrow())
                    }
                }) {
                    Text("This is a test")
                }
            }
        }
    }
    //TODO implement viewer
}