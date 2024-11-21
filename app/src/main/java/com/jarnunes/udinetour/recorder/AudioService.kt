package com.jarnunes.udinetour.recorder

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.helper.FileHelper
import java.io.File

class AudioService(private val mainActivity: MainActivity) {

    private var audioFile: File? = null

    private val recorder by lazy {
        AndroidAudioRecorder(mainActivity.applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(mainActivity.applicationContext)
    }

    fun record(afterStopRecordCallback: (File?) -> Unit, afterStartRecordCallback: () -> Unit) {
        val recordPermission = Manifest.permission.RECORD_AUDIO

        if (ActivityCompat.checkSelfPermission(
                mainActivity.applicationContext, recordPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(mainActivity, arrayOf(recordPermission), 200)
            return
        }

        // Start or stop recording
        if (recorder.isRecording()) {
            recorder.stop()
            audioFile?.let { afterStopRecordCallback(it) }
        } else {
            audioFile = FileHelper().createAudioFile(mainActivity)
            recorder.start(audioFile!!)
            afterStartRecordCallback()
        }
    }


}