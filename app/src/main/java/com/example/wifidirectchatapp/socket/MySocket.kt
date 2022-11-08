package com.example.wifidirectchatapp.socket

import com.example.wifidirectchatapp.my_socket_library.SingleSocket
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacketBuilder
import com.example.wifidirectchatapp.my_socket_library.IO
import java.io.File
import java.io.FileNotFoundException

class MySocket : SingleSocket() {
    fun emitMessage(message: String?) {
        val builder = MessagePacketBuilder()
        builder.setEvent(IO.SEND_MESSAGE)
        builder.setType(IO.SEND_MESSAGE)
        builder.setMessage(message)
        emit(builder.build())
    }

    fun emitFile(filePath: String?, filename: String?) {
        val file = File(filePath.toString())
        val builder = MessagePacketBuilder()
        builder.setMessage(filename)
        builder.setEvent(IO.SEND_FILE)
        builder.setType(IO.SEND_FILE)
        try {
            builder.setDataFromFile(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        emit(builder.build())
    }
}