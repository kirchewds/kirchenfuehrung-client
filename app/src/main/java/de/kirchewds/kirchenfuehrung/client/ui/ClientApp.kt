package de.kirchewds.kirchenfuehrung.client.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.kirchewds.kirchenfuehrung.client.data.ToursRepository
import de.kirchewds.kirchenfuehrung.client.ui.theme.AppTheme

@Composable
fun ClientApp(
    repository: ToursRepository,
    navController: NavHostController
) {
    AppTheme {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp.value
        val screenHeight = configuration.screenHeightDp.dp.value

        ClientNavGraph(
            repository = repository,
            isExpandedScreen = screenWidth > screenHeight,
            navController = navController
        )
    }
}

/**
 * Determine the content padding to apply to the different screens of the app
 */
@Composable
fun rememberContentPaddingForScreen(
    additionalTop: Dp = 0.dp,
    excludeTop: Boolean = false
) =
    WindowInsets.systemBars
        .only(if (excludeTop) WindowInsetsSides.Bottom else WindowInsetsSides.Vertical)
        .add(WindowInsets(top = additionalTop))
        .asPaddingValues()