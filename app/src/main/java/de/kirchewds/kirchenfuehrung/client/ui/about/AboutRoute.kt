package de.kirchewds.kirchenfuehrung.client.ui.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import de.kirchewds.kirchenfuehrung.client.R
import de.kirchewds.kirchenfuehrung.client.ui.Wordmark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutRoute() {
    val topAppBarState = rememberTopAppBarState()
    Scaffold(
        topBar = {
            AboutTopBar(topAppBarState = topAppBarState)
        },
        modifier = Modifier
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val uriHandler = LocalUriHandler.current
                val clipboardManager = LocalClipboardManager.current
                val context = LocalContext.current
                val bugAddress = "contact-project+kirchewds-kirchenfuehrung-client-60715858-issue-@incoming.gitlab.com"
                val legalText = stringResource(R.string.about_legal_text)

                ActionTile(
                    icon = { Image(painter = painterResource(id = R.drawable.logo_netzgottesdienst), null, modifier = Modifier.size(24.dp)) },
                    title = { Text(stringResource(R.string.about_netzgottesdienst)) },
                    subtitle = { Text(stringResource(R.string.about_netzgottesdienst_text)) },
                    action = { uriHandler.openUri("http://www.netzgottesdienst.de") }
                )
                ActionTile(
                    icon = { Image(painter = painterResource(id = R.drawable.logo_kirche), null, modifier = Modifier.size(24.dp)) },
                    title = { Text(stringResource(R.string.about_legal)) },
                    subtitle = { Text(legalText) },
                    action = { clipboardManager.setText(AnnotatedString(legalText)) }
                )
                HorizontalDivider()
                ActionTile(
                    icon = { Image(painter = painterResource(id = R.drawable.logo_nann_stiftung), null, modifier = Modifier.size(24.dp)) },
                    title = { },
                    subtitle = { Text(stringResource(id = R.string.about_sponsor)) },
                    action = { }
                )
                HorizontalDivider()
                ActionTile(
                    icon = { Icon(Icons.Default.Person, null) },
                    title = { Text(stringResource(R.string.about_author)) },
                    action = { uriHandler.openUri("https://jfronny.gitlab.io/contact.html") }
                )
                ActionTile(
                    icon = { Icon(Icons.Default.BugReport, null) },
                    title = { Text(stringResource(R.string.about_bugs)) },
//                    subtitle = { Text(address) },
                    action = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            this.data = "mailto:".toUri()
                            this.putExtra(Intent.EXTRA_EMAIL, arrayOf(bugAddress))
                            this.putExtra(Intent.EXTRA_SUBJECT, "Bug report")
                        })
                    }
                )
                ActionTile(
                    icon = { Icon(Icons.Default.Code, null) },
                    title = { Text(stringResource(R.string.about_code)) },
                    action = { uriHandler.openUri("https://gitlab.com/kirchewds/kirchenfuehrung-client") }
                )
            }
        }
    }
}

@Composable
fun ActionTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = {},
    action: () -> Unit
) {
    Card(
//        colors = SettingsTileDefaults.cardColors(),
        onClick = action
    ) {
        ListItem(
//            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = icon,
            headlineContent = title,
            supportingContent = subtitle
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutTopBar(topAppBarState: TopAppBarState = rememberTopAppBarState()) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    CenterAlignedTopAppBar(
        title = { Wordmark() },
        scrollBehavior = scrollBehavior,
        modifier = Modifier
    )
}