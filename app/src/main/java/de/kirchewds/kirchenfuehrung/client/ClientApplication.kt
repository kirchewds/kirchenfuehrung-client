package de.kirchewds.kirchenfuehrung.client

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClientApplication: Application() {
    companion object {
        const val URI_SCHEME = "kirchenfuehrung"

        const val TOUR_URI = "$URI_SCHEME://"
        const val TOURS_JSON_URI = "https://kirchewds.github.io/kirchenfuehrung-data/tours.json"
    }
}