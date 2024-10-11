package de.kirchewds.kirchenfuehrung.client.util

import androidx.media3.common.MediaItem
import de.kirchewds.kirchenfuehrung.client.model.Track

val MediaItem.metadata: Track? get() = localConfiguration?.tag as? Track