package de.kirchewds.kirchenfuehrung.client.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun <R, T> Result<T>.flatMap(transform: (value: T) -> Result<R>): Result<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when {
        isSuccess -> transform(getOrThrow())
        else -> Result.failure(exceptionOrNull()!!)
    }
}

fun <T> Result.Companion.ofNullable(value: T?): Result<T> = if (value == null) failure(NullPointerException()) else success(value)