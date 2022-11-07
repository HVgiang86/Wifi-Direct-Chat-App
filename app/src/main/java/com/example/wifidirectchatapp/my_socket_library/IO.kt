package com.example.wifidirectchatapp.my_socket_library

import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket

interface IO {
    companion object {
        const val SEND_MESSAGE = "send_message"
        const val SEND_FILE = "send_file"
        const val CLIENT_DISCONNECT = "disconnect"
    }

    interface OnConnectListener {
        fun onConnect(socket: SingleSocket?)
    }

    interface OnDisconnectListener {
        fun onDisconnect(socket: SingleSocket?)
    }

    interface OnNewMessageListener {
        fun onNewMessage(socket: SingleSocket?, MessagePacket: MessagePacket?)
    }
}