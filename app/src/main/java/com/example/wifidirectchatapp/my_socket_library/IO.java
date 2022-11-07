package com.example.wifidirectchatapp.my_socket_library;

import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket;

public interface IO {
    String SEND_MESSAGE = "send_message";
    String SEND_FILE = "send_file";
    String CLIENT_DISCONNECT = "disconnect";
    interface OnConnectListener{
        void onConnect(SingleSocket socket);
    }

    interface OnDisconnectListener{
        void onDisconnect(SingleSocket socket);
    }

    interface OnNewMessageListener{
        void onNewMessage(SingleSocket socket, MessagePacket MessagePacket);
    }
}
