package de.kirchewds.kirchenfuehrung.client.ui.overview

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.kirchewds.kirchenfuehrung.client.R
import de.kirchewds.kirchenfuehrung.client.model.Cookie
import de.kirchewds.kirchenfuehrung.client.model.Tour
import de.kirchewds.kirchenfuehrung.client.ui.ClientNavigationActions
import de.kirchewds.kirchenfuehrung.client.ui.LoadingAnimation
import de.kirchewds.kirchenfuehrung.client.ui.Wordmark
import de.kirchewds.kirchenfuehrung.client.ui.components.ClientSnackbarHost
import de.kirchewds.kirchenfuehrung.client.ui.components.pullrefresh.PullRefreshIndicator
import de.kirchewds.kirchenfuehrung.client.ui.components.pullrefresh.pullRefresh
import de.kirchewds.kirchenfuehrung.client.ui.components.pullrefresh.rememberPullRefreshState
import de.kirchewds.kirchenfuehrung.client.ui.rememberContentPaddingForScreen
import de.kirchewds.kirchenfuehrung.client.util.ErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewRoute(
    overviewViewModel: OverviewViewModel,
    isExpandedScreen: Boolean,
    navigation: ClientNavigationActions,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by overviewViewModel.uiState.collectAsStateWithLifecycle()

    val overviewListLazyListState = rememberLazyListState()
    val showTopAppBar = true
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        snackbarHost = { ClientSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (showTopAppBar) {
                OverviewTopAppBar(topAppBarState = topAppBarState, onSelectAbout = navigation::navigateToAbout)
            }
        },
        modifier = Modifier
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .nestedScroll(scrollBehavior.nestedScrollConnection)

        val pullRefreshState = rememberPullRefreshState(refreshing = uiState is OverviewUiState.Loading, onRefresh = overviewViewModel::refreshTours)

        Box(Modifier.pullRefresh(pullRefreshState)) {
            when (val state = uiState) {
                is OverviewUiState.Tours -> {
                    if (state.other.isEmpty()) {
                        SingleToursList(
                            tour = state.highlighted,
                            onTourTapped = navigation::navigateToTour,
                            modifier = contentModifier,
                            isExpandedScreen = isExpandedScreen
                        )
                    } else {
                        MultiToursList(
                            highlighted = state.highlighted,
                            tours = state.other,
                            onTourTapped = navigation::navigateToTour,
                            contentPadding = rememberContentPaddingForScreen(
                                additionalTop = if (showTopAppBar) 0.dp else 8.dp,
                                excludeTop = showTopAppBar
                            ),
                            modifier = contentModifier,
                            state = overviewListLazyListState
                        )
                    }
                }
                is OverviewUiState.Error -> {
                    Box(contentModifier.fillMaxSize()) {}
                    val errorMessage = remember(state) { state.errorMessages[0] }

                    val onRefreshToursState by rememberUpdatedState(overviewViewModel::refreshTours)
                    val onErrorDismissState by rememberUpdatedState(overviewViewModel::errorShown)

                    ErrorDialog(message = errorMessage, dismiss = onErrorDismissState, retry = onRefreshToursState)
                }
                OverviewUiState.Empty -> {
                    Box(contentModifier.fillMaxSize()) {}
                }
                OverviewUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = contentModifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars)
                    ) {
                        LoadingAnimation(MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            PullRefreshIndicator(pullRefreshState.refreshing, pullRefreshState, Modifier.align(
                Alignment.TopCenter))
        }
    }

    val cookie = navigation.cookie
    if (cookie is Cookie.Feedback) {
        FeedbackDialog(feedback = cookie, dismiss = { navigation.cookie = Cookie.None })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverviewTopAppBar(
    topAppBarState: TopAppBarState = rememberTopAppBarState(),
    onSelectAbout: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    CenterAlignedTopAppBar(
        title = { Wordmark() },
        actions = {
            IconButton(onClick = onSelectAbout) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.about)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = Modifier
    )
}

@Composable
fun TourTitle(tour: Tour) {
    Text(
        text = tour.name,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun FeedbackDialog(feedback: Cookie.Feedback, dismiss: () -> Unit) {
    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties()
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
        ) {
            Column {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.feedback_title),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.feedback_message),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    horizontalArrangement = Arrangement.SpaceAround) {

                    TextButton(onClick = dismiss) {
                        Text(
                            text = stringResource(R.string.feedback_dismiss),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                        )
                    }
                    val context = LocalContext.current
                    val subject = stringResource(id = R.string.feedback_subject)
                    TextButton(onClick = {
                        dismiss()
                        context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("contact-project+kirchewds-kirchenfuehrung-data-60715903-issue-@incoming.gitlab.com"))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                        })
                    }) {
                        Text(
                            stringResource(R.string.feedback_send),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorDialog(message: ErrorMessage, dismiss: (Long) -> Unit, retry: () -> Unit) {
    val dismiss = { dismiss(message.id) }
    val retry = { dismiss(); retry() }
    Dialog(onDismissRequest = dismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
        ) {
            Column {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.error_title),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(message.messageId),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    horizontalArrangement = Arrangement.SpaceAround) {
                    TextButton(onClick = retry) {
                        Text(
                            stringResource(id = R.string.retry),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                        )
                    }
                }
            }
        }
    }
}