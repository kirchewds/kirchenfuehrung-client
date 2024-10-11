package de.kirchewds.kirchenfuehrung.client.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class Tour(val name: String, val description: String, val cover: String?, val tracks: List<Track>): Serializable
