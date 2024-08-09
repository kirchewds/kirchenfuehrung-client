package io.gitlab.jfronny.kirchenfuehrung.client.ui.overview

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun OverviewRoute(
    overviewViewModel: OverviewViewModel,
    isExpandedScreen: Boolean,
    navigation: ClientNavigationActions,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by overviewViewModel.uiState.collectAsStateWithLifecycle()

    OverviewRoute(
        uiState = uiState,
        isExpandedScreen = isExpandedScreen,
        onErrorDismiss = overviewViewModel::errorShown,
        onRefresh = overviewViewModel::refreshTours,
        onSelectTour = navigation::navigateToTour,
        onSelectAbout = navigation::navigateToAbout,
        snackbarHostState = snackbarHostState,
        cookie = navigation.cookie,
        setCookie = { navigation.cookie = it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewRoute(
    uiState: OverviewUiState,
    isExpandedScreen: Boolean,
    onErrorDismiss: (Long) -> Unit,
    onRefresh: () -> Unit,
    onSelectTour: (String) -> Unit,
    onSelectAbout: () -> Unit,
    snackbarHostState: SnackbarHostState,
    cookie: Cookie,
    setCookie: (Cookie) -> Unit
) {
    val overviewListLazyListState = rememberLazyListState()
    val showTopAppBar = true
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    Scaffold(
        snackbarHost = { ClientSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (showTopAppBar) {
                OverviewTopAppBar(topAppBarState = topAppBarState, onSelectAbout = onSelectAbout)
            }
        },
        modifier = Modifier
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .nestedScroll(scrollBehavior.nestedScrollConnection)

        val pullRefreshState = rememberPullRefreshState(refreshing = uiState is OverviewUiState.Loading, onRefresh = onRefresh)

        Box(Modifier.pullRefresh(pullRefreshState)) {
            when (uiState) {
                is OverviewUiState.Tours -> {
                    ToursList(
                        highlighted = uiState.highlighted,
                        tours = uiState.other,
                        onTourTapped = onSelectTour,
                        contentPadding = rememberContentPaddingForScreen(
                            additionalTop = if (showTopAppBar) 0.dp else 8.dp,
                            excludeTop = showTopAppBar
                        ),
                        modifier = contentModifier,
                        state = overviewListLazyListState,
                    )
                }
                is OverviewUiState.Error -> {
                    Box(contentModifier.fillMaxSize()) {}
                    val errorMessage = remember(uiState) { uiState.errorMessages[0] }

                    val errorMessageText: String = stringResource(errorMessage.messageId)
                    val retryMessageText = stringResource(id = R.string.retry)

                    val onRefreshToursState by rememberUpdatedState(onRefresh)
                    val onErrorDismissState by rememberUpdatedState(onErrorDismiss)

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

            PullRefreshIndicator(pullRefreshState.refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }

    if (cookie is Cookie.Feedback) {
        FeedbackDialog(feedback = cookie, dismiss = { setCookie(Cookie.None) })
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
private fun ToursList(
    highlighted: Tour,
    tours: List<Tour>,
    onTourTapped: (id: String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    state: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        state = state
    ) {
        item { ToursListTopSection(highlighted, onTourTapped) }
        if (tours.isNotEmpty()) {
            item { ToursListSection(
                tours,
                onTourTapped
            ) }
        }
    }
}

@Composable
fun ToursListTopSection(highlighted: Tour, navigateToTour: (id: String) -> Unit) {
    TourCardTop(
        tour = highlighted,
        modifier = Modifier.clickable(onClick = { navigateToTour(highlighted.name) })
    )
    ToursListDivider()
}

@Composable
fun ToursListSection(tours: List<Tour>, navigateToTour: (id: String) -> Unit) {
    Column {
        tours.forEach {
            TourCardSimple(
                tour = it,
                navigateToTour = navigateToTour
            )
            ToursListDivider()
        }
    }
}

@Composable
fun TourCardSimple(
    tour: Tour,
    navigateToTour: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = { navigateToTour(tour.name) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TourImage(tour, Modifier
            .padding(16.dp)
            .size(40.dp, 40.dp)
            .clip(MaterialTheme.shapes.small))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            TourTitle(tour)
        }
    }
}

@Composable
private fun ToursListDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
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
fun TourCardTop(tour: Tour, modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TourImage(tour, Modifier
            .heightIn(max = 180.dp)
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.medium), ContentScale.Crop)
        Spacer(Modifier.height(16.dp))

        Text(
            text = tour.name,
            style = typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
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
                        context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("contact-project+kirchewds-kirchenfuehrung-data-60715903-issue-@incoming.gitlab.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "${feedback.keyword} for ${feedback.track.tour.name}/${feedback.track.name}")
                        })
                    }, modifier = Modifier.padding(10.dp)) {
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