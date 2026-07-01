package dev.mobase.core

import dev.mobase.common.Initializer

interface AppInitializer : Initializer {
    suspend fun start(): StartupData
}