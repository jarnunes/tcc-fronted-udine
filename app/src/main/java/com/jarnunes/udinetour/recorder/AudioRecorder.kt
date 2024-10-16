package com.jarnunes.udinetour.recorder

import java.io.File

interface AudioRecorder {

    fun start(outputFile: File)
    fun stop()
    fun isRecording(): Boolean
}