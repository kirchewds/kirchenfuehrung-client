package de.kirchewds.kirchenfuehrung.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import de.kirchewds.kirchenfuehrung.client.data.ToursRepository
import de.kirchewds.kirchenfuehrung.client.ui.about.AboutRoute
import de.kirchewds.kirchenfuehrung.client.ui.overview.OverviewRoute
import de.kirchewds.kirchenfuehrung.client.ui.overview.OverviewViewModel
import de.kirchewds.kirchenfuehrung.client.ui.viewer.LocalPlayerConnection
import de.kirchewds.kirchenfuehrung.client.ui.viewer.ViewerRoute
import de.kirchewds.kirchenfuehrung.client.ui.viewer.ViewerViewModel

@Composable
fun ClientNavGraph(
    repository: ToursRepository,
    isExpandedScreen: Boolean,
    navController: Navigator,
) {
    val entryProvider = entryProvider {
        entry<ClientDestinations.Overview> {
            val overviewViewModel: OverviewViewModel = viewModel(
                factory = OverviewViewModel.provideFactory(
                    toursRepository = repository
                )
            )
            OverviewRoute(
                overviewViewModel = overviewViewModel,
                isExpandedScreen = isExpandedScreen,
                navigation = navController
            )
        }
        entry<ClientDestinations.About> {
            AboutRoute()
        }
        entry<ClientDestinations.Viewer> {
            val tourId = it.id
            val viewerViewModel: ViewerViewModel = viewModel(
                factory = ViewerViewModel.provideFactory(
                    repository,
                    tourId ?: LocalPlayerConnection.current?.currentTour,
                    tourId == null
                )
            )
            ViewerRoute(
                viewerViewModel = viewerViewModel,
                isExpandedScreen = isExpandedScreen,
                navigation = navController
            )
        }
    }

    NavDisplay(
        entries = navController.toEntries(entryProvider),
        onBack = { navController.navigateBack() },
        sceneStrategy = remember { DialogSceneStrategy() }
    )
}