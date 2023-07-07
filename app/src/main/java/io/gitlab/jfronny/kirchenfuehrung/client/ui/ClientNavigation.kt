package io.gitlab.jfronny.kirchenfuehrung.client.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object ClientDestinations {
    const val OVERVIEW = "overview"
    const val VIEWER = "viewer"

    const val TOUR_ID = "tour"
}

class ClientNavigationActions(navController: NavHostController) {
    val navigateToOverview: () -> Unit = {
        navController.navigate(ClientDestinations.OVERVIEW) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    val navigateToInterests: () -> Unit = {
        navController.navigate(ClientDestinations.VIEWER) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}