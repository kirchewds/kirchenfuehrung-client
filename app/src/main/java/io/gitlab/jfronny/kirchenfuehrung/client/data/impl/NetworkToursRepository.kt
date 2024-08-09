package io.gitlab.jfronny.kirchenfuehrung.client.data.impl

import io.gitlab.jfronny.gson.GsonBuilder
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NetworkToursRepository(private val toursJsonUrl: HttpUrl): AbstractToursRepository() {
    private val gson = GsonBuilder().serializeNulls().create()
    private val client = OkHttpClient()
    private val mutex = Mutex()
    private var tours: Tours? = null

    override suspend fun getTours(): Result<Tours> = mutex.withLock {
        if (tours != null) Result.success(tours!!)
        else withContext(Dispatchers.IO) {
            try {
                val response = client.execute(Request.Builder().url(toursJsonUrl).build())
                if (!response.isSuccessful) throw IOException("Unexpected code: $response")
                response.body!!.byteStream().use {
                    it.reader().use {
                        gson.newJsonReader(it).use {
                            val t = ToursParser.parseTours(it)
                            if (t.isSuccess) tours = t.getOrThrow()
                            t
                        }
                    }
                }
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    private suspend fun OkHttpClient.execute(request: Request): Response = suspendCancellableCoroutine { continuation ->
        val call = newCall(request)

        call.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (call.isCanceled()) return
                continuation.resume(response)
            }
        })

        continuation.invokeOnCancellation {
            call.cancel()
        }
    }
}
