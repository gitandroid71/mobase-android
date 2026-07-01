package dev.mobase.core.event

import kotlinx.coroutines.CoroutineScope

internal interface EventObserver {
    fun launch(scope: CoroutineScope)
}