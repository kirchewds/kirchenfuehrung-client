package de.kirchewds.kirchenfuehrung.client.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import de.kirchewds.kirchenfuehrung.client.model.Cookie

object ClientDestinations {
    const val OVERVIEW = "overview"
    const val VIEWER = "viewer"
    const val ABOUT = "about"

    const val TOUR_ID = "tour"
}

class ClientNavigationActions(private val navController: NavHostController, cookie: MutableState<Cookie>) {
    var cookie by cookie

    fun navigateToOverview(cookie: Cookie = Cookie.None) {
        this.cookie = cookie
        navController.navigate(ClientDestinations.OVERVIEW) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToTour(id: String, cookie: Cookie = Cookie.None) {
        this.cookie = cookie
        navController.navigate("${ClientDestinations.VIEWER}/$id") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToAbout(cookie: Cookie = Cookie.None) {
        this.cookie = cookie
        navController.navigate(ClientDestinations.ABOUT) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateBack(cookie: Cookie = Cookie.None) {
        if (navController.popBackStack()) this.cookie = cookie
    }
}