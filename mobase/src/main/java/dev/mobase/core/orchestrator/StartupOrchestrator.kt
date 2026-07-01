package dev.mobase.core.orchestrator

import dev.mobase.core.StartupData

internal interface StartupOrchestrator {
    suspend fun start(): StartupData
}