package io.gitlab.jfronny.kirchenfuehrung.client.data.impl

import io.gitlab.jfronny.commons.serialize.gson.api.v1.GsonHolders
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tours
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.StringReader

class NetworkToursRepository(private val toursJsonUrl: Url): AbstractToursRepository() {
    private val gson = GsonHolders.API.modifyBuilder { it.serializeNulls() }.gson
    private val client = HttpClient(OkHttp)
    private val mutex = Mutex()
    private var tours: Tours? = null

    override suspend fun getTours(): Result<Tours> = mutex.withLock {
        if (tours != null) Result.success(tours!!)
        else withContext(Dispatchers.IO) {
            try {
                StringReader(client.get<String>(toursJsonUrl)).use {
                    gson.newJsonReader(it).use {
                        val t = ToursParser.parseTours(it)
                        if (t.isSuccess) tours = t.getOrThrow()
                        t
                    }
                }
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }
}
