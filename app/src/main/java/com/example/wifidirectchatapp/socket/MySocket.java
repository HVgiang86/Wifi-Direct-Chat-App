package com.example.wifidirectchatapp.socket;

import com.example.wifidirectchatapp.my_socket_library.IO;
import com.example.wifidirectchatapp.my_socket_library.SingleSocket;
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacketBuilder;

import java.io.File;
import java.io.FileNotFoundException;

public class MySocket extends SingleSocket {

    public void emitMessage(String username, String message) {
        MessagePacketBuilder builder = new MessagePacketBuilder();
        builder.setEvent(IO.SEND_MESSAGE);
        builder.setSender(username);
        builder.setType(IO.SEND_MESSAGE);
        builder.setMessage(message);
        emit(builder.build());
    }

    public void emitFile(String username, String filePath, String filename) {
        File file = new File(filePath);
        MessagePacketBuilder builder = new MessagePacketBuilder();
        builder.setMessage(filename);
        builder.setEvent(IO.SEND_FILE);
        builder.setType(IO.SEND_FILE);
        builder.setSender(username);
        try {
            builder.setDataFromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        emit(builder.build());
    }
}
