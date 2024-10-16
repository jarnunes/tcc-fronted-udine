package com.jarnunes.udinetour.recorder

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class AndroidAudioPlayer(private var context: Context) : AudioPlayer {

    private var player: MediaPlayer? = null

    override fun playFile(file: File) {
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath) // Configura o caminho do arquivo diretamente
            prepare() // Prepara o MediaPlayer
            start() // Inicia a reprodução
        }

//        MediaPlayer.create(context, file.toUri()).apply {
//            player = this
//            start()
//        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    override fun isPlaying(): Boolean {
        return player != null
    }
}