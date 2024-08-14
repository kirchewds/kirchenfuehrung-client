package io.gitlab.jfronny.kirchenfuehrung.client.ui.overview

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.gitlab.jfronny.kirchenfuehrung.client.R
import io.gitlab.jfronny.kirchenfuehrung.client.model.Cookie
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.ui.ClientNavigationActions
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.ClientSnackbarHost
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.PullRefreshIndicator
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.pullRefresh
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.rememberPullRefreshState
import io.gitlab.jfronny.kirchenfuehrung.client.ui.rememberContentPaddingForScreen

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

                    val errorMessageText: String = stringResource(errorMessage.messageId)
                    val retryMessageText = stringResource(id = R.string.retry)

                    val onRefreshToursState by rememberUpdatedState(overviewViewModel::refreshTours)
                    val onErrorDismissState by rememberUpdatedState(overviewViewModel::errorShown)

                    LaunchedEffect(errorMessageText, retryMessageText, snackbarHostState) {
                        val snackbarResult = snackbarHostState.showSnackbar(
                            message = errorMessageText,
                            actionLabel = retryMessageText
                        )
                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                            onRefreshToursState()
                        }
                        onErrorDismissState(errorMessage.id)
                    }
                }
                OverviewUiState.Empty, OverviewUiState.Loading -> {
                    Box(contentModifier.fillMaxSize()) {}
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
    val context = LocalContext.current
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
fun TourImage(tour: Tour, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit) {
    if (tour.cover == null) {
        Image(
            painter = painterResource(R.drawable.ic_client_placeholder),
            contentDescription = null, // decorative
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )
    } else {
        AsyncImage(
            model = tour.cover,
            contentDescription = null, //decorative
            contentScale = contentScale,
            modifier = modifier
        )
    }
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
                    TextButton(onClick = {
                        dismiss()
                        context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("contact-project+kirchewds-kirchenfuehrung-data-60715903-issue-@incoming.gitlab.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "${feedback.keyword} for ${feedback.track.tour.name}/${feedback.track.name}")
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