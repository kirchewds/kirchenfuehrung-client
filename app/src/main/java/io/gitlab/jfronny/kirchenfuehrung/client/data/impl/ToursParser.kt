package io.gitlab.jfronny.kirchenfuehrung.client.data.impl

import io.gitlab.jfronny.gson.stream.JsonReader
import io.gitlab.jfronny.gson.stream.JsonToken
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tours
import io.gitlab.jfronny.kirchenfuehrung.client.model.Track
import io.ktor.http.Url

object ToursParser {
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
                    else -> throw IllegalStateException("Unsupported tours json entry: $k")
                }
            }
            reader.endObject()
            if (highlight == null) throw IllegalStateException("Tours lacks highlight")
            if (tours.isNullOrEmpty()) throw IllegalStateException("Tours lacks tours")
            val highlighted = tours.lastOrNull { it.name == highlight } ?: throw IllegalStateException("Highlight does not exist")
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
                else -> throw IllegalStateException("Unsupported tour json entry: $k")
            }
        }
        reader.endObject()
        if (name == null) throw IllegalStateException("Tour lacks name")
        if (tracks.isNullOrEmpty()) throw IllegalStateException("Tour lacks tracks")
        return Tour(name, if (cover == null) null else Url(cover), tracks)
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
                else -> throw IllegalStateException("Unsupported tour json entry: $k")
            }
        }
        reader.endObject()
        if (name == null) throw IllegalStateException("Track lacks name")
        if (image == null) throw IllegalStateException("Track lacks image")
        if (audio == null) throw IllegalStateException("Track lacks audio")
        return Track(name, Url(image), Url(audio))
    }
}