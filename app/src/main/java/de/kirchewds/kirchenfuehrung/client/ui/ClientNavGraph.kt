package de.kirchewds.kirchenfuehrung.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.kirchewds.kirchenfuehrung.client.ClientApplication.Companion.TOUR_URI
import de.kirchewds.kirchenfuehrung.client.data.ToursRepository
import de.kirchewds.kirchenfuehrung.client.model.Cookie
import de.kirchewds.kirchenfuehrung.client.ui.ClientDestinations.TOUR_ID
import de.kirchewds.kirchenfuehrung.client.ui.about.AboutRoute
import de.kirchewds.kirchenfuehrung.client.ui.overview.OverviewRoute
import de.kirchewds.kirchenfuehrung.client.ui.overview.OverviewViewModel
import de.kirchewds.kirchenfuehrung.client.ui.viewer.ViewerRoute
import de.kirchewds.kirchenfuehrung.client.ui.viewer.ViewerViewModel

@Composable
fun ClientNavGraph(
    repository: ToursRepository,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ClientDestinations.OVERVIEW
) {
    val cookie = remember { mutableStateOf<Cookie>(Cookie.None) }
    val navigationActions = remember(navController, cookie) { ClientNavigationActions(navController, cookie) }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = ClientDestinations.OVERVIEW) {
            val overviewViewModel: OverviewViewModel = viewModel(
                factory = OverviewViewModel.provideFactory(
                    toursRepository = repository
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
                factory = ViewerViewModel.provideFactory(repository, tourId)
            )
            ViewerRoute(
                viewerViewModel = viewerViewModel,
                isExpandedScreen = isExpandedScreen,
                navigation = navigationActions
            )
        }
    }
}