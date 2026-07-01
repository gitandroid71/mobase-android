package dev.mobase.core

import dev.mobase.Mobase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import dev.mobase.common.Initializer
import dev.mobase.common.coroutines.Dispatchers
import dev.mobase.core.event.EventObserver
import dev.mobase.core.orchestrator.StartupOrchestrator
import java.util.concurrent.atomic.AtomicBoolean

internal class MobaseApplication(
    private val initializers: List<Initializer>,
    private val eventObservers: List<EventObserver>,
    private val startupOrchestrator: StartupOrchestrator,
    dispatchers: Dispatchers = Dispatchers()
) : Mobase {
    private val isInitialized = AtomicBoolean(false)
    private val isStarted = AtomicBoolean(false)
    private val startupCompletion = CompletableDeferred<StartupData>()

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)

    override fun initialize() {
        if (isInitialized.compareAndSet(false, true)) {
            initializers.forEach(Initializer::initialize)
            eventObservers.forEach { it.launch(scope) }
        }
    }

    override suspend fun start(): StartupData {
        if (isStarted.compareAndSet(false, true)) {
            try {
                startupCompletion.complete(startupOrchestrator.start())
            } catch (e: Throwable) {
                startupCompletion.completeExceptionally(e)
            }
        }

        return startupCompletion.await()
    }
}