package com.example.wifidirectchatapp.my_socket_library.model

import com.example.wifidirectchatapp.my_socket_library.IO
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class MessagePacketBuilder {
    private var sender: String? = null
    private var event: String? = null
    private var isFile = false
    private var message: String? = null
    private var data: ByteArray? = null
    private var dataSizeInByte = 0

    companion object {
        const val MAX_FILE_SIZE_IN_BYTE = 50 * 1024 * 1024
    }

    fun setEvent(event: String?) {
        this.event = event
    }

    fun setType(type: String) {
        isFile =
            type.equals("file", ignoreCase = true) || type.equals(IO.SEND_FILE, ignoreCase = true)
    }

    fun setSender(sender: String?) {
        this.sender = sender
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun setData(data: ByteArray?) {
        this.data = data
    }

    fun setDataSizeInByte(dataSizeInByte: Int) {
        this.dataSizeInByte = dataSizeInByte
    }

    @Throws(FileNotFoundException::class)
    fun setDataFromFile(file: File): Boolean {
        dataSizeInByte = file.length().toInt()
        message = file.name
        data = ByteArray(dataSizeInByte)

        //Max size limit of file is 50MB
        //Use file input stream to read file from storage and convert it into an array of bytes
        if (dataSizeInByte > MAX_FILE_SIZE_IN_BYTE) {
            throw FileNotFoundException("File size > 50Mb")
        }
        try {
            data = ByteArray(dataSizeInByte)
            val fis = FileInputStream(file)
            fis.read(data)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun build(): MessagePacket {
        return if (isFile) {
            MessagePacket(sender!!, event!!, true, message!!, data, dataSizeInByte)
        } else MessagePacket(sender!!, event!!, false, message!!)
    }

}