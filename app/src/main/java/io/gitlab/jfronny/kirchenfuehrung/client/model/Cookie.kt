package io.gitlab.jfronny.kirchenfuehrung.client.model

sealed interface Cookie {
    data object None: Cookie
    sealed interface Feedback : Cookie {
        val track: Track
        class Gesture(override val track: Track): Feedback
        class Finished(override val track: Track): Feedback
    }
}