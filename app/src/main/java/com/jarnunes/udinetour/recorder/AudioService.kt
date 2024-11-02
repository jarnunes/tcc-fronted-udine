package com.jarnunes.udinetour.recorder

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.jarnunes.udinetour.MainActivity
import java.io.File

class AudioService(private val mainActivity: MainActivity) {

    private var audioFile: File? = null

    private val recorder by lazy {
        AndroidAudioRecorder(mainActivity.applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(mainActivity.applicationContext)
    }


    fun record(
        afterStopRecordCallback: (File?) -> Unit,
        afterStartRecordCallback: () -> Unit
    ) {
        val recordPermission = Manifest.permission.RECORD_AUDIO

        // request permission
        if (ActivityCompat.checkSelfPermission(
                mainActivity.applicationContext, recordPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(mainActivity, arrayOf(recordPermission), 200)
        }


        // Start or stop recording
        if (recorder.isRecording()) {
            recorder.stop()
            audioFile?.let { afterStopRecordCallback(it) }
        } else {
            // defines file path
            audioFile = File(mainActivity.filesDir, "audio_${System.currentTimeMillis()}.mp4")

            // start record
            recorder.start(audioFile!!)

            afterStartRecordCallback()
        }

    }


}