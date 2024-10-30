package com.jarnunes.udinetour.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.message.Message
import com.jarnunes.udinetour.message.MessageType
import java.io.File

class AudioService(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var audioFile: File? = null


}