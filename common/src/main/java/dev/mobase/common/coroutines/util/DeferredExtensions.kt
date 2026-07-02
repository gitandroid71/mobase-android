package dev.mobase.common.coroutines.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Deferred<T>.getOrNull(): T? {
    return if (isCompleted) {
        try {
            getCompleted()
        } catch (e: Throwable) {
            null
        }
    } else {
        null
    }
}