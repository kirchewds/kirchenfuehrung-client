package io.gitlab.jfronny.kirchenfuehrung.client.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.gitlab.jfronny.kirchenfuehrung.client.R
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.ui.WebImage

@Composable
fun SingleToursList(
    tour: Tour,
    isExpandedScreen: Boolean,
    onTourTapped: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isExpandedScreen) ExpandedList(tour = tour, onTourTapped = onTourTapped, modifier = modifier)
    else PhoneList(tour = tour, onTourTapped = onTourTapped, modifier = modifier)
}

@Composable
fun ExpandedList(tour: Tour, onTourTapped: (id: String) -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            WebImage(url = tour.cover, modifier = Modifier.clip(RoundedCornerShape(6.dp)))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
        ) {
            Spacer(Modifier.weight(1f))
            ControlsContent(tour, onTourTapped)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun PhoneList(tour: Tour, onTourTapped: (id: String) -> Unit, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            WebImage(url = tour.cover, modifier = Modifier.clip(RoundedCornerShape(6.dp)))
        }

        ControlsContent(tour, onTourTapped)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ControlsContent(tour: Tour, onTourTapped: (id: String) -> Unit) {
    Text(
        text = tour.name,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = tour.description,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(Modifier.height(24.dp))

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onTourTapped(tour.name) }
    ) {
        Image(
            painter = painterResource(R.drawable.arrow_forward),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .align(Alignment.Center)
                .size(36.dp)
        )
    }
}