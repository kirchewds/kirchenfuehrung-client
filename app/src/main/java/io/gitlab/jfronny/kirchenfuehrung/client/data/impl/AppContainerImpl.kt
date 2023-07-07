package io.gitlab.jfronny.kirchenfuehrung.client.data.impl

import android.content.Context
import io.gitlab.jfronny.kirchenfuehrung.client.data.AppContainer
import io.ktor.http.Url

class AppContainerImpl(private val applicationContext: Context, toursJsonUri: String): AppContainer {
    override val toursRepository by lazy {
        NetworkToursRepository(Url(toursJsonUri))
    }
}