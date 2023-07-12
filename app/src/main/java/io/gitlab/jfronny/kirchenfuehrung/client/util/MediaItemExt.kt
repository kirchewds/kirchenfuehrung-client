package io.gitlab.jfronny.kirchenfuehrung.client.util

import androidx.media3.common.MediaItem
import io.gitlab.jfronny.kirchenfuehrung.client.model.Track

val MediaItem.metadata: Track? get() = localConfiguration?.tag as? Track