package io.gitlab.jfronny.kirchenfuehrung.client.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object ClientDestinations {
    const val OVERVIEW = "overview"
    const val VIEWER = "viewer"
    const val ABOUT = "about"

    const val TOUR_ID = "tour"
}

class ClientNavigationActions(private val navController: NavHostController) {
    fun navigateToOverview() {
        navController.navigate(ClientDestinations.OVERVIEW) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToTour(id: String) {
        navController.navigate("${ClientDestinations.VIEWER}/$id") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToAbout() {
        navController.navigate(ClientDestinations.ABOUT) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}