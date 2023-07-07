package io.gitlab.jfronny.kirchenfuehrung.client

import android.app.Application
import io.gitlab.jfronny.kirchenfuehrung.client.data.AppContainer
import io.gitlab.jfronny.kirchenfuehrung.client.data.impl.AppContainerImpl

class ClientApplication: Application() {
    companion object {
        const val TOUR_URI = "kirchenfuehrung://"
        const val TOURS_JSON_URI = "http://192.168.178.57:8080/tours.json"
    }

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this, TOURS_JSON_URI)
    }
}