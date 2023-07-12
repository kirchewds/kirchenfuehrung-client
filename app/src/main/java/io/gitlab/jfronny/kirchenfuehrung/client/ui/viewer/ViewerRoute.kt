package io.gitlab.jfronny.kirchenfuehrung.client.ui.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import coil.compose.AsyncImage
import io.gitlab.jfronny.kirchenfuehrung.client.R
import io.gitlab.jfronny.kirchenfuehrung.client.model.Track
import io.gitlab.jfronny.kirchenfuehrung.client.playback.PlayerConnection
import io.gitlab.jfronny.kirchenfuehrung.client.ui.ClientNavigationActions
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.ClientSnackbarHost
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.ResizableIconButton
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.PullRefreshIndicator
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.pullRefresh
import io.gitlab.jfronny.kirchenfuehrung.client.ui.components.pullrefresh.rememberPullRefreshState
import io.gitlab.jfronny.kirchenfuehrung.client.util.makeTimeString
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ViewerRoute(
    viewerViewModel: ViewerViewModel,
    isExpandedScreen: Boolean,
    navigation: ClientNavigationActions,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewerViewModel.uiState.collectAsStateWithLifecycle()

    ViewerRoute(
        uiState = uiState,
        isExpandedScreen = isExpandedScreen,
        onErrorDismiss = { viewerViewModel.errorShown(it) },
        onRefresh = { viewerViewModel.refreshTour() },
        onBack = { navigation.navigateBack() },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerRoute(
    uiState: ViewerUiState,
    isExpandedScreen: Boolean,
    onErrorDismiss: (Long) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val currentTrack by playerConnection.currentTrack.collectAsState(initial = null)
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    var initial by rememberSaveable { mutableStateOf(true) }

    BackHandler {
        playerConnection.stop()
        onBack()
    }

    Scaffold(
        snackbarHost = { ClientSnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .nestedScroll(scrollBehavior.nestedScrollConnection)

        val pullRefreshState = rememberPullRefreshState(refreshing = uiState is ViewerUiState.Loading, onRefresh = {
            initial = true
            onRefresh()
        })

        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .pullRefresh(pullRefreshState)) {
            when (uiState) {
                is ViewerUiState.Play -> {
                    if (initial) {
                        initial = false
                        playerConnection.play(uiState.tour)
                    }
                    if (currentTrack == null) {
                        Box(contentModifier.fillMaxSize())
                    } else {
                        if (isExpandedScreen) ExpandedPlayer(currentTrack!!, playerConnection)
                        else PhonePlayer(currentTrack!!, playerConnection)
                    }
                }
                is ViewerUiState.Error -> {
                    initial = true
                    playerConnection.stop()
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
                ViewerUiState.Loading -> {
                    initial = true
                    playerConnection.stop()
                    Box(contentModifier.fillMaxSize())
                }
            }

            PullRefreshIndicator(pullRefreshState.refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun ExpandedPlayer(track: Track, playerConnection: PlayerConnection) {
    Row(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            Thumbnail(track)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
        ) {
            Spacer(Modifier.weight(1f))
            ControlsContent(track, playerConnection)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun PhonePlayer(track: Track, playerConnection: PlayerConnection) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            Thumbnail(track)
        }

        ControlsContent(track, playerConnection)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun Thumbnail(track: Track) {
    AsyncImage(
        model = track.image.toString(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    )
}

@Composable
fun ControlsContent(track: Track, playerConnection: PlayerConnection) {
    Text(
        text = track.name,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(Modifier.height(12.dp))

    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    var position by rememberSaveable(playbackState) {
        mutableStateOf(playerConnection.player.currentPosition)
    }
    var duration by rememberSaveable(playbackState) {
        mutableStateOf(playerConnection.player.duration)
    }
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }
    var showHeadphonesScreen = rememberSaveable(playbackState) {
        mutableStateOf(false)
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(500)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    Slider(
        value = (sliderPosition ?: position).toFloat(),
        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
        onValueChange = {
            sliderPosition = it.toLong()
        },
        onValueChangeFinished = {
            sliderPosition?.let {
                playerConnection.player.seekTo(it)
                position = it
            }
            sliderPosition = null
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = makeTimeString(sliderPosition ?: position),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    Spacer(Modifier.height(12.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        Box(modifier = Modifier.weight(1f)) {
            ResizableIconButton(
                icon = R.drawable.skip_previous,
                enabled = canSkipPrevious,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center),
                onClick = playerConnection.player::seekToPrevious
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable {
                    if (playbackState == STATE_ENDED) {
                        playerConnection.player.run {
                            seekTo(0, 0)
                            if (playerConnection.isUsingHeadphones) playWhenReady = true
                            else showHeadphonesScreen.value = true
                        }
                    } else {
                        playerConnection.player.run {
                            if (playWhenReady) playWhenReady = false
                            else if (playerConnection.isUsingHeadphones) playWhenReady = true
                            else showHeadphonesScreen.value = true
                        }
                    }
                }
        ) {
            Image(
                painter = painterResource(if (playbackState == STATE_ENDED) R.drawable.replay else if (isPlaying) R.drawable.pause else R.drawable.play),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            ResizableIconButton(
                icon = R.drawable.skip_next,
                enabled = canSkipNext,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center),
                onClick = playerConnection.player::seekToNext
            )
        }
    }

    if (showHeadphonesScreen.value) {
        HeadphonesDialog(showHeadphonesScreen, playerConnection)
    }
}

@Composable
fun HeadphonesDialog(showHeadphonesScreen: MutableState<Boolean>, playerConnection: PlayerConnection, modifier: Modifier = Modifier) {
    Dialog(
        onDismissRequest = { showHeadphonesScreen.value = false },
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
        ) {
            Column {
                //.......................................................................
                Image(
                    painter = painterResource(id = R.drawable.headset),
                    contentDescription = null, // decorative
                    contentScale = ContentScale.Fit,
                    colorFilter  = ColorFilter.tint(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .padding(top = 35.dp)
                        .height(70.dp)
                        .fillMaxWidth(),

                    )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.headphones_title),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.headphones_description),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                //.......................................................................
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    horizontalArrangement = Arrangement.SpaceAround) {

                    TextButton(onClick = {
                        showHeadphonesScreen.value = false
                        playerConnection.player.playWhenReady = true
                    }) {

                        Text(
                            stringResource(R.string.headphones_not_now),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                        )
                    }
                    TextButton(onClick = {
                        if (playerConnection.isUsingHeadphones) {
                            showHeadphonesScreen.value = false
                            playerConnection.player.playWhenReady = true
                        }
                    }) {
                        Text(
                            stringResource(R.string.headphones_check_again),
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
