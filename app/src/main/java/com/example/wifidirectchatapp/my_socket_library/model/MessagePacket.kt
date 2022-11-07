package com.example.wifidirectchatapp.my_socket_library.model

class MessagePacket {
    val sender: String
    val event: String
    val isFile: Boolean
    val message: String
    val data: ByteArray?
    val dataSizeInByte: Int

    constructor(sender: String, event: String, isFile: Boolean, message: String) {
        this.event = event
        this.isFile = isFile
        this.message = message
        data = null
        dataSizeInByte = 0
        this.sender = sender
    }

    constructor(
        sender: String,
        event: String,
        isFile: Boolean,
        message: String,
        data: ByteArray?,
        dataSizeInByte: Int
    ) {
        this.event = event
        this.isFile = isFile
        this.data = data
        this.dataSizeInByte = dataSizeInByte
        this.message = message
        this.sender = sender
    }
}