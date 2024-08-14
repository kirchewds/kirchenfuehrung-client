package io.gitlab.jfronny.kirchenfuehrung.client.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline

interface SimplePlayerListener : Player.Listener {
    fun updateTimeline()

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = updateTimeline()
    override fun onTimelineChanged(timeline: Timeline, reason: Int) = updateTimeline()
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = updateTimeline()
    override fun onRepeatModeChanged(repeatMode: Int) = updateTimeline()
}