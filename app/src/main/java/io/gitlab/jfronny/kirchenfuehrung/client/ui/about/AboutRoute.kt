package io.gitlab.jfronny.kirchenfuehrung.client.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.gitlab.jfronny.kirchenfuehrung.client.R

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
                Text(
                    text = stringResource(R.string.about),
                    style = typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
                )
                LinkTile(
                    icon = {
                        Icon(Icons.Default.Person, null)
                    },
                    title = {
                        Text(stringResource(R.string.about_author))
                    },
                    url = "https://jfronny.gitlab.io/contact.html"
                )
                LinkTile(
                    icon = {
                        Icon(Icons.Default.BugReport, null)
                    },
                    title = {
                        Text(stringResource(R.string.about_bugs))
                    },
                    url = "" //TODO fill in
                )
                LinkTile(
                    icon = {
                        Icon(Icons.Default.Code, null)
                    },
                    title = {
                        Text(stringResource(R.string.about_code))
                    },
                    url = "" //TODO fill in
                )
                //TODO more details about who made this
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    url: String
) {
    val handler = LocalUriHandler.current
    Card(
//        colors = SettingsTileDefaults.cardColors(),
        onClick = {
            handler.openUri(url)
            //context.symphony.shorty.startBrowserActivity(context.activity, url)
            // ^- alias to activity.startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
        }
    ) {
        ListItem(
//            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineContent = { title() },
            supportingContent = { Text(url) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutTopBar(topAppBarState: TopAppBarState = rememberTopAppBarState()) {
    val title = stringResource(id = R.string.app_name)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.ic_client_wordmark),
                contentDescription = title,
                contentScale = ContentScale.Inside,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth()
            )
        },
        scrollBehavior = scrollBehavior,
        modifier = Modifier
    )
}