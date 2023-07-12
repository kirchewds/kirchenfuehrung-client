package io.gitlab.jfronny.kirchenfuehrung.client.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class Tour(val name: String, val cover: String?, val tracks: List<Track>): Serializable
