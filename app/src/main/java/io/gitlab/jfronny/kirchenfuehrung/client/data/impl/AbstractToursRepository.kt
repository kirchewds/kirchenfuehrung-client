package io.gitlab.jfronny.kirchenfuehrung.client.data.impl

import io.gitlab.jfronny.kirchenfuehrung.client.ClientApplication
import io.gitlab.jfronny.kirchenfuehrung.client.data.ToursRepository
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.model.Track
import io.gitlab.jfronny.kirchenfuehrung.client.util.flatMap
import io.gitlab.jfronny.kirchenfuehrung.client.util.ofNullable

abstract class AbstractToursRepository: ToursRepository {
    override suspend fun getHighlighted(): Result<Tour> = getTours().map { it.highlight }
    override suspend fun getSecondaryTours(): Result<Map<String, Tour>> = getTours().map { it.secondary }
    override suspend fun getTour(id: String): Result<Tour> =
        getAllTours().mapCatching { it[id] ?: throw IllegalArgumentException("Tour not found") }
    private suspend fun getAllTours(): Result<Map<String, Tour>> = getTours()
        .map { listOf(it.highlight.name to it.highlight) + it.secondary.toList() }
        .map { it.toMap() }

    override suspend fun getTrack(id: String): Result<Track> {
        val split = id.substring(ClientApplication.TOUR_URI.length).split('/')
        return getTour(split[0]).flatMap { Result.ofNullable(it.tracks.firstOrNull { it.name == split[1] }) }
    }
}