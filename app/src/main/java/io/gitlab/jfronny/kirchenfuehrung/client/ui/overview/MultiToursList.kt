package io.gitlab.jfronny.kirchenfuehrung.client.ui.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour

@Composable
fun MultiToursList(
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
private fun ToursListTopSection(highlighted: Tour, navigateToTour: (id: String) -> Unit) {
    TourCardTop(
        tour = highlighted,
        modifier = Modifier.clickable(onClick = { navigateToTour(highlighted.name) })
    )
    ToursListDivider()
}

@Composable
private fun ToursListSection(tours: List<Tour>, navigateToTour: (id: String) -> Unit) {
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
private fun ToursListDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}

@Composable
private fun TourCardSimple(
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
private fun TourCardTop(tour: Tour, modifier: Modifier = Modifier) {
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
