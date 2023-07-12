package io.gitlab.jfronny.kirchenfuehrung.client.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class Tours(val highlight: Tour, val secondary: Map<String, Tour>): Serializable
