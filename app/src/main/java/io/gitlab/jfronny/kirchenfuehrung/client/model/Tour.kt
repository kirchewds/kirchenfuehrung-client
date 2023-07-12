package io.gitlab.jfronny.kirchenfuehrung.client.model

import androidx.compose.runtime.Immutable
import io.ktor.http.Url
import java.io.Serializable

@Immutable
data class Tour(val name: String, val cover: Url?, val tracks: List<Track>): Serializable
