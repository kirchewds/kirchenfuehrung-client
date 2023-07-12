package io.gitlab.jfronny.kirchenfuehrung.client.util

import androidx.media3.common.Player
import io.gitlab.jfronny.kirchenfuehrung.client.model.Track

val Player.currentMetadata: Track? get() = currentMediaItem?.metadata