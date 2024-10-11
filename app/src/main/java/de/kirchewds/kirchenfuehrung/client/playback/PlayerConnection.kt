package de.kirchewds.kirchenfuehrung.client.playback

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Timeline
import de.kirchewds.kirchenfuehrung.client.model.Tour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PlayerConnection(
    binder: MediaPlaybackService.MusicBinder,
    scope: CoroutineScope
): SimplePlayerListener {
    val service = binder.service
    val player = service.player

    val playbackState = MutableStateFlow(player.playbackState)
    val playWhenReady = MutableStateFlow(player.playWhenReady)
    val isPlaying = combine(playbackState, playWhenReady) { playbackState, playWhenReady ->
        playWhenReady && playbackState != STATE_ENDED
    }.stateIn(scope, SharingStarted.Lazily, player.playWhenReady && player.playbackState != STATE_ENDED)
    val currentTrack = service.currentMediaMetadata

    val isInitialTrack = MutableStateFlow(true)
    val isFinalTrack = MutableStateFlow(true)

    val error = MutableStateFlow<PlaybackException?>(null)

    init {
        player.addListener(this)

        playbackState.value = player.playbackState
        playWhenReady.value = player.playWhenReady

        updateTimeline()
    }

    fun play(item: Tour) = service.play(item)
    fun stop() = service.stop()
    val isUsingHeadphones get() = service.isUsingHeadphones

    override fun onPlaybackStateChanged(state: Int) {
        playbackState.value = state
        error.value = player.playerError
    }

    override fun onPlayWhenReadyChanged(newPlayWhenReady: Boolean, reason: Int) {
        playWhenReady.value = newPlayWhenReady
    }

    override fun onPlayerErrorChanged(playbackError: PlaybackException?) {
        error.value = playbackError
    }

    override fun updateTimeline() {
        if (!player.currentTimeline.isEmpty) {
            val window = player.currentTimeline.getWindow(player.currentMediaItemIndex, Timeline.Window())
            isInitialTrack.value = !player.isCommandAvailable(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    && !window.isLive
                    && !player.isCommandAvailable(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            isFinalTrack.value = (!window.isLive || !window.isDynamic)
                    && !player.isCommandAvailable(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
        } else {
            isInitialTrack.value = true
            isFinalTrack.value = true
        }
    }

    fun dispose() {
        player.removeListener(this)
    }
}