package io.gitlab.jfronny.kirchenfuehrung.client.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import io.gitlab.jfronny.kirchenfuehrung.client.R
import kotlin.math.roundToInt

@Composable
fun Wordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
//        Image(
//            painter = painterResource(id = R.drawable.ic_launcher_foreground),
//            contentDescription = stringResource(id = R.string.app_name),
//            modifier = Modifier.size(48.dp)
//        )
        Text(stringResource(id = R.string.app_name), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun WebImage(url: String?, modifier: Modifier = Modifier.clip(RoundedCornerShape(6.dp)), contentScale: ContentScale = ContentScale.Fit) {
    val painter = rememberAsyncImagePainter(url)
    val state = painter.state
    if (state is AsyncImagePainter.State.Error) {
        Image(
            painter = painterResource(R.drawable.ic_client_placeholder),
            contentDescription = null, // decorative
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )
    } else {
        if (state is AsyncImagePainter.State.Loading) {
            LoadingAnimation(MaterialTheme.colorScheme.onBackground)
        }

        var zoom by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp.value
        val screenHeight = configuration.screenHeightDp.dp.value

        val zoomableModifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer(scaleX = zoom, scaleY = zoom)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                    if (zoom > 1) {
                        val dx = pan.x * zoom
                        val dy = pan.y * zoom
                        offsetX = (offsetX + dx).coerceIn(-(screenWidth * zoom)..(screenWidth * zoom))
                        offsetY = (offsetY + dy).coerceIn(-(screenHeight * zoom)..(screenHeight * zoom))
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }

        val transition by animateFloatAsState(if (state is AsyncImagePainter.State.Success) 1f else 0f)
        Image(
            painter = painter,
            contentDescription = null, // decorative
            contentScale = contentScale,
            modifier = zoomableModifier.alpha(transition)
        )
    }
}

@Composable
fun LoadingAnimation(color: Color) {
    val animation = rememberInfiniteTransition()
    val progress by animation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart,
        )
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(progress)
            .alpha(1f - progress)
            .border(
                5.dp,
                color = color,
                shape = CircleShape
            )
    )
}