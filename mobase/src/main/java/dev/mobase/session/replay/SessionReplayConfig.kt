package dev.mobase.session.replay

data class SessionReplayConfig(
    val sampleRate: Number = 0.0,
    val autoStart: Boolean = false,
    val enableLogging: Boolean
)
