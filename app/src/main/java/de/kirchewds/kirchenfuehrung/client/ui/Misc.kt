package de.kirchewds.kirchenfuehrung.client.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import de.kirchewds.kirchenfuehrung.client.R

@Composable
fun Wordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
//        Image(
//            painter = painterResource(id = R.drawable.ic_launcher_foreground),
//            contentDescription = stringResource(id = R.string.app_name),
//            modifier = Modifier.size(48.dp)
//        )
        Text(stringResource(id = R.string.app_title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
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
        var offset by remember { mutableStateOf(Offset(0f, 0f)) }

        var frameSize: IntSize by remember { mutableStateOf(IntSize(0, 0)) }

        val zoomableModifier = modifier
            .graphicsLayer(scaleX = zoom, scaleY = zoom, translationX = offset.x, translationY = offset.y)
            .onSizeChanged { frameSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                    if (zoom > 1) {
                        val maxOffset = Offset(
                            frameSize.width * (zoom - 1) / 2,
                            frameSize.height * (zoom - 1) / 2
                        )

                        offset = Offset(
                            (offset.x + pan.x * zoom).coerceIn(-maxOffset.x..maxOffset.x),
                            (offset.y + pan.y * zoom).coerceIn(-maxOffset.y..maxOffset.y)
                        )
                    } else {
                        offset = Offset.Zero
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