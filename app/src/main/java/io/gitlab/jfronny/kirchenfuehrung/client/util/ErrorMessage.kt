package io.gitlab.jfronny.kirchenfuehrung.client.util

import androidx.annotation.StringRes
import java.util.UUID

data class ErrorMessage(val id: Long, @StringRes val messageId: Int) {
    constructor(@StringRes messageId: Int): this(UUID.randomUUID().mostSignificantBits, messageId)
}
