package com.jarnunes.udinetour.helper

import android.content.Context
import com.jarnunes.udinetour.model.Message
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
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
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return ArrayList()
        } catch (e: WriteAbortedException) {
            e.printStackTrace()
            return ArrayList()
        }
    }
}