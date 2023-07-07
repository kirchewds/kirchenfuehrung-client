package io.gitlab.jfronny.kirchenfuehrung.client.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import io.gitlab.jfronny.kirchenfuehrung.client.R
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.ui.ClientDestinations
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.ClientSnackbarHost
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.PullRefreshIndicator
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.pullRefresh
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.rememberPullRefreshState
import io.gitlab.jfronny.kirchenfuehrung.client.ui.rememberContentPaddingForScreen

@Composable
fun OverviewRoute(
    overviewViewModel: OverviewViewModel,
    isExpandedScreen: Boolean,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by overviewViewModel.uiState.collectAsStateWithLifecycle()

    OverviewRoute(
        uiState = uiState,
        isExpandedScreen = isExpandedScreen,
        onErrorDismiss = { overviewViewModel.errorShown(it) },
        onRefresh = { overviewViewModel.refreshTours() },
        onSelectTour = { navController.navigate("${ClientDestinations.VIEWER}/$it") },
        snackbarHostState = snackbarHostState
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
    snackbarHostState: SnackbarHostState
) {
    val overviewListLazyListState = rememberLazyListState()
    val showTopAppBar = true
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    Scaffold(
        snackbarHost = { ClientSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (showTopAppBar) {
                OverviewTopAppBar(topAppBarState = topAppBarState)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverviewTopAppBar(topAppBarState: TopAppBarState = rememberTopAppBarState()) {
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
            IconButton(onClick = {

            }) {
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
            .clickable(onClick = { navigateToTour(tour.name) })
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
    Divider(
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
        val imageModifier = Modifier
            .heightIn(max = 180.dp)
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.medium)
        TourImage(tour, imageModifier, ContentScale.Crop)
        Spacer(Modifier.height(16.dp))

        Text(
            text = tour.name,
            style = typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}