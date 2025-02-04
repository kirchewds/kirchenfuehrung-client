package de.kirchewds.kirchenfuehrung.client.data

import de.kirchewds.kirchenfuehrung.client.model.Tour
import de.kirchewds.kirchenfuehrung.client.model.Tours
import de.kirchewds.kirchenfuehrung.client.model.Track

interface ToursRepository {
    suspend fun getTours(): Result<Tours>
    suspend fun getHighlighted(): Result<Tour>
    suspend fun getSecondaryTours(): Result<Map<String, Tour>>
    suspend fun getTour(id: String): Result<Tour>
    suspend fun getTrack(id: String): Result<Track>
}