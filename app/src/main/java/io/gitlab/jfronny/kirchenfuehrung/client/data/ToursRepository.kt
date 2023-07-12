package io.gitlab.jfronny.kirchenfuehrung.client.data

import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tours
import io.gitlab.jfronny.kirchenfuehrung.client.model.Track

interface ToursRepository {
    suspend fun getTours(): Result<Tours>
    suspend fun getHighlighted(): Result<Tour>
    suspend fun getSecondaryTours(): Result<Map<String, Tour>>
    suspend fun getTour(id: String): Result<Tour>
    suspend fun getTrack(id: String): Result<Track>
}