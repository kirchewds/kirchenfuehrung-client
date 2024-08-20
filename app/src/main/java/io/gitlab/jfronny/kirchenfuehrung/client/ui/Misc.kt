package io.gitlab.jfronny.kirchenfuehrung.client.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.gitlab.jfronny.kirchenfuehrung.client.R

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