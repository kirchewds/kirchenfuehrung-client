package io.gitlab.jfronny.kirchenfuehrung.client.data.impl

import android.util.Log
import androidx.core.net.toUri
import io.gitlab.jfronny.gson.stream.JsonReader
import io.gitlab.jfronny.gson.stream.JsonToken
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tours
import io.gitlab.jfronny.kirchenfuehrung.client.model.Track

object ToursParser {
    private val TAG = "ToursParser"

    fun parseTours(reader: JsonReader): Result<Tours> {
        try {
            var highlight: String? = null
            var tours: List<Tour>? = null
            reader.beginObject()
            if (reader.nextName() != "version") throw IllegalStateException("Expected version")
            if (reader.nextInt() != 1) throw IllegalStateException("Unsupported tracks format version")
            while (reader.peek() != JsonToken.END_OBJECT) {
                when (val k = reader.nextName()) {
                    "highlight" -> highlight = reader.nextString()
                    "tours" -> tours = parseToursArray(reader)
                    else -> {
                        reader.skipValue()
                        Log.e(TAG, "parseTours: Unsupported tours json entry: $k")
                    }
                }
            }
            reader.endObject()
            if (tours.isNullOrEmpty()) throw IllegalStateException("Tours lacks tours")
            if (highlight == null) {
                Log.e(TAG, "parseTours: Tours lacks highlight. Using first tour as highlight")
                highlight = tours.first().name
            }
            val highlighted = tours.lastOrNull { it.name == highlight } ?: run {
                Log.e(TAG, "parseTours: Highlight does not exist. Using first tour as highlight")
                tours.first()
            }
            val secondary = tours.associateBy { it.name }.filterKeys { it != highlight }
            return Result.success(Tours(highlighted, secondary))
        } catch (e: IllegalStateException) {
            return Result.failure(e)
        }
    }

    private fun parseToursArray(reader: JsonReader): List<Tour> {
        val tours = ArrayList<Tour>()
        reader.beginArray()
        while (reader.peek() != JsonToken.END_ARRAY) {
            tours.add(parseTour(reader))
        }
        reader.endArray()
        return tours
    }

    private fun parseTour(reader: JsonReader): Tour {
        var name: String? = null
        var cover: String? = null
        var tracks: List<Track>? = null
        reader.beginObject()
        while (reader.peek() != JsonToken.END_OBJECT) {
            when (val k = reader.nextName()) {
                "name" -> name = reader.nextString()
                "cover" -> cover = if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    null
                } else reader.nextString()
                "tracks" -> tracks = parseTracksArray(reader)
                else -> {
                    reader.skipValue()
                    Log.e(TAG, "parseTour: Unsupported tour json entry: $k")
                }
            }
        }
        reader.endObject()
        if (name == null) {
            Log.e(TAG, "parseTour: Tour lacks name. Using placeholder")
            name = "Unnamed Tour"
        }
        if (tracks.isNullOrEmpty()) throw IllegalStateException("Tour lacks tracks")
        val tour = Tour(name, cover, tracks)
        tracks.forEach { it.tour = tour }
        return tour
    }

    private fun parseTracksArray(reader: JsonReader): List<Track> {
        val tracks = ArrayList<Track>()
        reader.beginArray()
        while (reader.peek() != JsonToken.END_ARRAY) {
            tracks.add(parseTrack(reader))
        }
        reader.endArray()
        return tracks
    }

    private fun parseTrack(reader: JsonReader): Track {
        var name: String? = null
        var image: String? = null
        var audio: String? = null
        reader.beginObject()
        while (reader.peek() != JsonToken.END_OBJECT) {
            when (val k = reader.nextName()) {
                "name" -> name = reader.nextString()
                "image" -> image = reader.nextString()
                "audio" -> audio = reader.nextString()
                else -> {
                    reader.skipValue()
                    Log.e(TAG, "parseTrack: Unsupported tour json entry: $k")
                }
            }
        }
        reader.endObject()
        if (name == null) {
            Log.e(TAG, "parseTrack: Track lacks name. Using placeholder")
            name = "Unnamed Track"
        }
        if (image == null) Log.e(TAG, "parseTrack: Track lacks image")
        if (audio == null) throw IllegalStateException("Track lacks audio")
        return Track(name, image?.toUri(), audio.toUri())
    }
}