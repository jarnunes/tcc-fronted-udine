package com.jarnunes.udinetour.helper

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.commons.LogTag
import com.jarnunes.udinetour.message.Message
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InvalidClassException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.WriteAbortedException

class FileHelper {

    private val fileName = "messages.dat"

    fun writeMessages(item: ArrayList<Message>, context: Context) {
        try {
            val fileOutput: FileOutputStream =
                context.openFileOutput(fileName, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutput)
            objectOutputStream.writeObject(item)
            objectOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun readMessages(context: Context): ArrayList<Message> {
        try {
            val fileInputStream: FileInputStream = context.openFileInput(fileName)
            val objectInputStream = ObjectInputStream(fileInputStream)
            return objectInputStream.readObject() as ArrayList<Message>
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        } catch (e: WriteAbortedException) {
            e.printStackTrace()
            return ArrayList()
        } catch (e: InvalidClassException) {
            e.printStackTrace()
            return ArrayList()
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
    }

    fun createImageURI(context: Context): Uri {
        createFileDir(context, "images")

        val image = File(context.filesDir, "images/udine_tour_${System.currentTimeMillis()}.png")
        return FileProvider.getUriForFile(context, "com.jarnunes.udinetour.provider", image)
    }

    fun createAudioFile(context: Context): File {
        createFileDir(context, "audios")
        return File(context.filesDir, "audios/audio_${System.currentTimeMillis()}.mp4")
    }

    private fun createFileDir(context: Context, directoryName: String){
        val dir = File(context.filesDir, directoryName)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun createSampleAudioFile(context: Context): File {
        return File(context.filesDir, "MUSICA.mp4")
    }


    fun saveAudioToFile(context: Context, uri: Uri, audioData: ByteArray) {
        try {
            // Abre o OutputStream usando o ContentResolver
            val outputStream = context.contentResolver.openOutputStream(uri)

            // Verifica se o OutputStream não é nulo
            outputStream?.use {
                // Escreve os dados do áudio no arquivo
                it.write(audioData)
                it.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteFileByPath(context: Context, absolutePath: String): Boolean {
        return try {
            val file = File(absolutePath)
            if (file.exists()) {
                file.delete()
                Log.i(
                    LogTag.fileHelper(),
                    context.getString(R.string.success_delete_file, absolutePath)
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(
                LogTag.fileHelper(),
                context.getString(R.string.exception_delete_file, absolutePath, e.message)
            )
            false
        }
    }

}