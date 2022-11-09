package com.example.wifidirectchatapp.socket

import android.os.Environment
import android.util.Log
import com.example.wifidirectchatapp.model.Message
import com.example.wifidirectchatapp.model.MessageManager
import com.example.wifidirectchatapp.my_socket_library.IO
import com.example.wifidirectchatapp.my_socket_library.SingleSocket
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket
import java.io.*

class OnNewMessageListener : IO.OnNewMessageListener {
    override fun onNewMessage(socket: SingleSocket?, MessagePacket: MessagePacket?) {
        if (MessagePacket!!.isFile) {
            saveFileFromStream(MessagePacket)
        } else {
            if (MessagePacket.event.equals(IO.SEND_MESSAGE, ignoreCase = true)) {
                val message = Message(MessagePacket.message, isServer = false, isFile = false)
                MessageManager.getInstance().addMessage(message)
            } else {
                socket?.onDisconnectListener?.onDisconnect(socket)
            }
        }
    }

    private fun saveFileFromStream(message: MessagePacket?) {
        val filename = message!!.message
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.d(TAG, "File name: " + message.message)
        Log.d(TAG, "File path: " + dir.path)
        if (dir.exists()) {
            dir.mkdirs()
        }
        try {
            val file = File(dir.path, filename)
            val bytes = message.data
            val fos = FileOutputStream(file)
            val bos = BufferedOutputStream(fos)
            bos.write(bytes)
            bos.close()
            val msg = Message(file.path, isServer = false, isFile = true)
            MessageManager.getInstance().addMessage(msg)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "New Message"
    }
}