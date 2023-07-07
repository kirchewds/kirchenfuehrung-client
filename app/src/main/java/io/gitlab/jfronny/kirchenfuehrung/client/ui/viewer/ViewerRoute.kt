package io.gitlab.jfronny.kirchenfuehrung.client.ui.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.gitlab.jfronny.kirchenfuehrung.client.ui.ClientNavigationActions

@Composable
fun ViewerRoute(
    viewerViewModel: ViewerViewModel,
    isExpandedScreen: Boolean,
    navigation: ClientNavigationActions,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = Modifier
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(text = viewerViewModel.getTourId())
        }
    }
    //TODO implement viewer
}