package io.gitlab.jfronny.kirchenfuehrung.client.model

import io.ktor.http.Url

data class Track(val name: String, val image: Url, val audio: Url)
