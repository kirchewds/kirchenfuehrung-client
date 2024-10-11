package de.kirchewds.kirchenfuehrung.client.model

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Immutable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.media3.common.util.UnstableApi
import de.kirchewds.kirchenfuehrung.client.ClientApplication
import java.io.Serializable

@Immutable
data class Track(val name: String, val image: Uri?, val audio: Uri): Serializable {
    lateinit var tour: Tour

    val id: String get() = "${ClientApplication.TOUR_URI}${tour.name}/$name"

    @OptIn(UnstableApi::class)
    fun toMediaItem() = MediaItem.Builder()
        .setMediaId(id)
        .setUri(id)
        .setCustomCacheKey(id)
        .setTag(this)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(name)
                .setAlbumTitle(tour.name)
                .setArtworkUri(image)
                .setMediaType(MEDIA_TYPE_MUSIC)
                .build()
        )
        .build()
}
