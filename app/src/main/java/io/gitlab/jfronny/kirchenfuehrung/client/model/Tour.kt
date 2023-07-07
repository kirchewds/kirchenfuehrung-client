package io.gitlab.jfronny.kirchenfuehrung.client.model

import io.ktor.http.Url

data class Tour(val name: String, val cover: Url?, val tracks: List<Track>)
