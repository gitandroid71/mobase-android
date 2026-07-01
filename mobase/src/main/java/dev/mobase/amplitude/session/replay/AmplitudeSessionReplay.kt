package dev.mobase.amplitude.session.replay

import com.amplitude.android.plugins.SessionReplayPlugin
import dev.mobase.amplitude.AmplitudeAnalytics
import dev.mobase.session.replay.SessionReplay
import dev.mobase.session.replay.SessionReplayConfig

internal class AmplitudeSessionReplay(
    private val amplitudeAnalytics: AmplitudeAnalytics,
    private val config: SessionReplayConfig,
) : SessionReplay {
    private val sessionReplayPlugin by lazy {
        SessionReplayPlugin(
            sampleRate = config.sampleRate,
            enableRemoteConfig = true,
            autoStart = config.autoStart
        )
    }

    private var isAdded = false

    @Synchronized
    override fun startRecording() {
        if (isAdded) return

        amplitudeAnalytics.amplitude.add(sessionReplayPlugin)
        isAdded = true
    }

    @Synchronized
    override fun stopRecording() {
        if (!isAdded) return

        amplitudeAnalytics.amplitude.remove(sessionReplayPlugin)
        isAdded = false
    }
}
