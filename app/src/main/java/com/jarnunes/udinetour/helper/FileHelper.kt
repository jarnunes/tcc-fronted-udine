package com.jarnunes.udinetour.helper

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.net.toUri
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.commons.ILog
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

    fun readFilesAsByteArray(filesName: List<String>): List<ByteArray>{
        val files = ArrayList<ByteArray>()
        filesName.mapNotNull { readFileAsByteArray(it) }.forEach { files.add(it) }
        return files
    }

    private fun readFileAsByteArray(filePath: String): ByteArray? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.readBytes()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createImageFile(fileName: String, content: String, context: Context): File {
        createFileDir(context, "images")
        val image = File(context.filesDir, "images/${fileName}")
        saveFile(context, image.toUri(), decodeBase64ToByteArray(content))
        return image
    }

    fun createAudioFile(context: Context): File {
        createFileDir(context, "audios")
        return File(context.filesDir, "audios/audio_${System.currentTimeMillis()}.mp3")
    }

    private fun createFileDir(context: Context, directoryName: String){
        val dir = File(context.filesDir, directoryName)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun createAudioFile(context: Context, base64String: String): File {
        val byteArray = decodeBase64ToByteArray(base64String)
        val file = createAudioFile(context)
        saveFile(context, file.toUri(), byteArray)

        return file
    }

    private fun decodeBase64ToByteArray(base64String: String): ByteArray {
        return Base64.decode(base64String, Base64.DEFAULT)
    }

    @SuppressLint("NewApi")
    fun encodeFileToBase64(audio: File): String {
        return java.util.Base64.getEncoder().encodeToString(audio.readBytes())
        //return Base64.encodeToString(audio.readBytes(), Base64.DEFAULT)
    }

    private fun saveFile(context: Context, uri: Uri, audioData: ByteArray) {
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
                    ILog.fileHelper(),
                    context.getString(R.string.success_delete_file, absolutePath)
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(
                ILog.fileHelper(),
                context.getString(R.string.exception_delete_file, absolutePath, e.message)
            )
            false
        }
    }

}