package com.example.wifidirectchatapp.my_socket_library.AsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class WriteAsyncTask extends AsyncTask<MessagePacket, Void, Void> {
    private final static String TAG = "Transferring Log";
    private final Socket mSocket;

    public WriteAsyncTask(Socket mSocket) {
        this.mSocket = mSocket;
    }

    @Override
    protected Void doInBackground(MessagePacket... MessagePackets) {
        //Here we send an message package to server
        //Message package's content will be sent in the following order: event -> sender -> fileSize (if file)
        // -> message -> data in byte array (if file), each properties will be sent on a new line

        //message package to send
        MessagePacket MessagePacket = MessagePackets[0];

        try {
            OutputStream os = mSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.flush();

            //write event
            dos.writeBytes(MessagePacket.getEvent() + "\n");

            //write sender name
            dos.writeUTF(MessagePacket.getSender() + '\n');

            if (MessagePacket.isFile()) {
                //write data size in byte
                dos.writeBytes(MessagePacket.getDataSizeInByte() + "\n");

                //write file name
                dos.writeBytes(MessagePacket.getMessage() + "\n");

                //write data in byte array
                byte[] data = MessagePacket.getData();
                os.write(data, 0, MessagePacket.getDataSizeInByte());
            } else {
                //write message
                dos.writeUTF(MessagePacket.getMessage() + "\n");
//                dos.writeBytes(MessagePacket.getMessage() + "\n");
            }

            Log.d(TAG, "transferred");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Data transferred fail!");
        }
        return null;
    }

}

