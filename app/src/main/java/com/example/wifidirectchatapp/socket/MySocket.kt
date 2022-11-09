package com.example.wifidirectchatapp.socket

import com.example.wifidirectchatapp.my_socket_library.SingleSocket
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacketBuilder
import com.example.wifidirectchatapp.my_socket_library.IO
import java.io.File
import java.io.FileNotFoundException

class MySocket : SingleSocket() {

    /**
     * This function used to emit an Message to socket's stream
     * @param message is the message that need to send
     */
    fun emitMessage(message: String?) {
        val builder = MessagePacketBuilder()
        builder.setEvent(IO.SEND_MESSAGE)
        builder.setType(IO.SEND_MESSAGE)
        builder.setMessage(message)
        emit(builder.build())
    }

    /**
     * This function used to emit an File to socket's stream
     * @param filePath is the filepath used to browse to that file
     * @param filename is the filename, it will be transfer as message content, it's help receiver know filename
     * @return void
     */
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