package com.example.wifidirectchatapp.my_socket_library;

import android.os.AsyncTask;

import com.example.wifidirectchatapp.my_socket_library.AsyncTask.ReadAsyncTask;
import com.example.wifidirectchatapp.my_socket_library.AsyncTask.WriteAsyncTask;
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket;
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacketBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class SingleSocket {
    private Socket socket;

    public IO.OnConnectListener onConnectListener;
    public IO.OnDisconnectListener disconnectListener;
    public IO.OnNewMessageListener newMessageListener;

    public void createSocket(int port) {
        socket = new Socket();
        try {
            socket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void connectSocket(Context context, String ip, String port) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        SocketConnectAsync async = new SocketConnectAsync(this, contextWeakReference);
        String[] strings = new String[2];
        strings[0] = ip;
        strings[1] = port;
        async.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, strings);
    }*/

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOnConnectListener(IO.OnConnectListener onConnectListener) {
        this.onConnectListener = onConnectListener;
    }

    public void setDisconnectListener(IO.OnDisconnectListener disconnectListener) {
        this.disconnectListener = disconnectListener;
    }

    public void setNewMessageListener(IO.OnNewMessageListener newMessageListener) {
        this.newMessageListener = newMessageListener;
    }

    public void startReadingStream() {
        ReadAsyncTask readAsyncTask = new ReadAsyncTask(this);
        readAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void emit(MessagePacket MessagePacket) {
        WriteAsyncTask writeAsyncTask = new WriteAsyncTask(socket);
        writeAsyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,MessagePacket);
    }

    public void onDestroy() {
        MessagePacketBuilder builder = new MessagePacketBuilder();
        builder.setEvent("disconnect");
        builder.setType("message");
        builder.setMessage("Disconnect");

        MessagePacket MessagePacket = builder.build();
        this.emit(MessagePacket);

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
