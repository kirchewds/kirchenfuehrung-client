package io.gitlab.jfronny.kirchenfuehrung.client

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClientApplication: Application() {
    companion object {
        const val TOUR_URI = "kirchenfuehrung://"
        const val TOURS_JSON_URI = "https://kirchewds.gitlab.io/kirchenfuehrung-data/tours.json"
    }
}