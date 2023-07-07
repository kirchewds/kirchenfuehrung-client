package io.gitlab.jfronny.kirchenfuehrung.client.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import io.gitlab.jfronny.kirchenfuehrung.client.ClientApplication.Companion.TOUR_URI
import io.gitlab.jfronny.kirchenfuehrung.client.data.AppContainer
import io.gitlab.jfronny.kirchenfuehrung.client.ui.ClientDestinations.TOUR_ID
import io.gitlab.jfronny.kirchenfuehrung.client.ui.about.AboutRoute
import io.gitlab.jfronny.kirchenfuehrung.client.ui.overview.OverviewRoute
import io.gitlab.jfronny.kirchenfuehrung.client.ui.overview.OverviewViewModel
import io.gitlab.jfronny.kirchenfuehrung.client.ui.viewer.ViewerRoute
import io.gitlab.jfronny.kirchenfuehrung.client.ui.viewer.ViewerViewModel

@Composable
fun ClientNavGraph(
    appContainer: AppContainer,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ClientDestinations.OVERVIEW
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        val navigationActions = ClientNavigationActions(navController)
        composable(route = ClientDestinations.OVERVIEW) {
            val overviewViewModel: OverviewViewModel = viewModel(
                factory = OverviewViewModel.provideFactory(
                    toursRepository = appContainer.toursRepository
                )
            )
            OverviewRoute(
                overviewViewModel = overviewViewModel,
                isExpandedScreen = isExpandedScreen,
                navigation = navigationActions
            )
        }
        composable(route = ClientDestinations.ABOUT) {
            AboutRoute()
        }
        composable(
            route = "${ClientDestinations.VIEWER}/{$TOUR_ID}",
            arguments = listOf(navArgument(TOUR_ID) { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "$TOUR_URI/${ClientDestinations.VIEWER}?$TOUR_ID={$TOUR_ID}"
                }
            )
        ) { navBackStackEntry ->
            val tourId = navBackStackEntry.arguments?.getString(TOUR_ID)
            val viewerViewModel: ViewerViewModel = viewModel(
                factory = ViewerViewModel.provideFactory(appContainer.toursRepository, tourId)
            )
            ViewerRoute(
                viewerViewModel = viewerViewModel,
                isExpandedScreen = isExpandedScreen,
                navigation = navigationActions
            )
        }
    }
}