package io.gitlab.jfronny.kirchenfuehrung.client.model

sealed interface Cookie {
    data object None: Cookie
    sealed interface Feedback : Cookie {
        val track: Track
        val keyword: String
        class Gesture(override val track: Track): Feedback {
            override val keyword: String get() = "Suggestion"
        }

        class Finished(override val track: Track): Feedback {
            override val keyword: String get() = "Feedback"
        }
    }
}