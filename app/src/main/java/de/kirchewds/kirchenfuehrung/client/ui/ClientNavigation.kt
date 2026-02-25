package de.kirchewds.kirchenfuehrung.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer
import de.kirchewds.kirchenfuehrung.client.model.Cookie
import kotlinx.serialization.Serializable

@Serializable sealed interface ClientDestinations : NavKey {
    @Serializable data object Overview : ClientDestinations
    @Serializable data class Viewer(val id: String?) : ClientDestinations
    @Serializable data object About : ClientDestinations
}

@Composable
fun rememberNavigator(startDestination: ClientDestinations): Navigator {
    val cookie = remember { mutableStateOf<Cookie>(Cookie.None) }
    val backStack = rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) {
        NavBackStack(startDestination).apply {
            if (startDestination != ClientDestinations.Overview) add(0, ClientDestinations.Overview)
        }
    }
    val navigator = remember(cookie) { Navigator(cookie, backStack) }
    return navigator
}

class Navigator(
    cookie: MutableState<Cookie>,
    val backStack: NavBackStack<ClientDestinations>
) {
    var cookie by cookie

    fun navigateToOverview(cookie: Cookie = Cookie.None) {
        this.cookie = cookie
        backStack.add(ClientDestinations.Overview)
    }

    fun navigateToTour(id: String, cookie: Cookie = Cookie.None) {
        this.cookie = cookie
        backStack.clear()
        backStack.add(ClientDestinations.Viewer(id))
    }

    fun navigateToAbout(cookie: Cookie = Cookie.None) {
        this.cookie = cookie
        backStack.add(ClientDestinations.About)
    }

    fun navigateBack(cookie: Cookie = Cookie.None) {
        val currentRoute = backStack.last()

        if (currentRoute != ClientDestinations.Overview){
            backStack.removeLastOrNull()
            this.cookie = cookie
        }
    }
}

@Composable
fun Navigator.toEntries(
    entryProvider: (ClientDestinations) -> NavEntry<ClientDestinations>
): SnapshotStateList<NavEntry<ClientDestinations>> = rememberDecoratedNavEntries(
    backStack = backStack,
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
    ),
    entryProvider = entryProvider
).toMutableStateList()
