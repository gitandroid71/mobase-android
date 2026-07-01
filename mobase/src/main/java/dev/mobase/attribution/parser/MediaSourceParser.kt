package dev.mobase.attribution.parser

import dev.mobase.attribution.model.MediaSource

interface MediaSourceParser {
    fun parse(value: String?): MediaSource

    companion object {
        fun create(): MediaSourceParser = DefaultMediaSourceParser()
    }
}