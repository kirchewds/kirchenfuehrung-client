package io.gitlab.jfronny.kirchenfuehrung.client.data.impl

import io.gitlab.jfronny.kirchenfuehrung.client.model.Tours
import io.ktor.http.Url
import kotlinx.coroutines.delay

class DelayedNetworkToursRepository(toursJsonUrl: Url): AbstractToursRepository() {
    private val delegate = NetworkToursRepository(toursJsonUrl)

    override suspend fun getTours(): Result<Tours> {
        delay(500)
        return delegate.getTours()
    }
}