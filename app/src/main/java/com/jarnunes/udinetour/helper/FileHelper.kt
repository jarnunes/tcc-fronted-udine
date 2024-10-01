package com.jarnunes.udinetour.helper

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.jarnunes.udinetour.model.Message
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
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
        } catch (e: Exception){
            e.printStackTrace()
            return ArrayList()
        }
    }

    fun createImageURI(context: Context): Uri {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdir()
        }

        val image = File(context.filesDir, "udine_tour_${System.currentTimeMillis()}.png")
        return FileProvider.getUriForFile(context, "com.jarnunes.udinetour.provider", image)
    }
}