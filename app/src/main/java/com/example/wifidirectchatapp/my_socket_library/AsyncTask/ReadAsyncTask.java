package com.example.wifidirectchatapp.my_socket_library.AsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import com.example.wifidirectchatapp.my_socket_library.IO;
import com.example.wifidirectchatapp.my_socket_library.SingleSocket;
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket;
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacketBuilder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadAsyncTask extends AsyncTask<Void, Void, Void> {
    private final static String TAG = "Data Received Tag";
    private final SingleSocket socket;

    public ReadAsyncTask(SingleSocket socket) {
        this.socket = socket;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //Here we looking for incoming data from socket
        //Every time has data on input stream, we will read it until end of stream
        //Incoming message package structured in the following order: event -> sender -> fileSize (if file)
        // -> message -> data in byte array (if file)
        //kep running this task until an DISCONNECT event was sent to
        try {
            while (true) {
                InputStream is = socket.getSocket().getInputStream();
                DataInputStream dis = new DataInputStream(is);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                Log.d(TAG, "New Data");
                MessagePacketBuilder builder = new MessagePacketBuilder();

                //read event
                String event = dis.readLine();
                builder.setEvent(event);

                //read sender name
                String sender = dis.readUTF();
                builder.setSender(sender);

                Log.d(TAG, "Event: " + event);
                Log.d(TAG, "Sender: " + sender);

                if (event.equalsIgnoreCase(IO.SEND_FILE)) {
                    builder.setType(IO.SEND_FILE);

                    //read file size
                    int fileSize = Integer.parseInt(dis.readLine());
                    Log.d(TAG, "file size: " + fileSize);

                    //read filename as message properties
                    String filename = dis.readLine();
                    Log.d(TAG, "Filename: " + filename);

                    //read data of file in byte array
                    byte[] bytes = new byte[fileSize];
                    //dis.read(bytes);
                    dis.readFully(bytes);
                    Log.d(TAG, "byte array done!");

                    builder.setMessage(filename);
                    builder.setData(bytes);
                    builder.setDataSizeInByte(fileSize);
                } else {
                    builder.setType(IO.SEND_MESSAGE);
                    //read message
                    String message = dis.readUTF();
                    //String message = br.readLine();
                    builder.setMessage(message);
                }

                MessagePacket MessagePacket = builder.build();
                Log.d(TAG, "Data received: " + MessagePacket);

                //if meet disconnect event, finish read task
                if (MessagePacket.getEvent().equalsIgnoreCase(IO.CLIENT_DISCONNECT)) {
                    socket.disconnectListener.onDisconnect(socket);
                    onDestroy();
                    return null;
                }

                socket.newMessageListener.onNewMessage(socket, MessagePacket);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void onDestroy() {
        try {
            socket.getSocket().close();
            socket.disconnectListener.onDisconnect(socket);
            Log.d("SERVER TAG", "Client disconnected!");
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
