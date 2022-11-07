@file:Suppress("DEPRECATION")

package com.example.wifidirectchatapp.my_socket_library

import android.os.AsyncTask
import com.example.wifidirectchatapp.my_socket_library.async_task.ReadAsyncTask
import com.example.wifidirectchatapp.my_socket_library.async_task.WriteAsyncTask
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacketBuilder
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

abstract class SingleSocket {
    /*public void connectSocket(Context context, String ip, String port) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        SocketConnectAsync async = new SocketConnectAsync(this, contextWeakReference);
        String[] strings = new String[2];
        strings[0] = ip;
        strings[1] = port;
        async.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, strings);
    }*/
    var socket: Socket? = null


    @JvmField
    var onConnectListener: IO.OnConnectListener? = null
    var onDisconnectListener: IO.OnDisconnectListener? = null
    var newMessageListener: IO.OnNewMessageListener? = null
    fun createSocket(port: Int) {
        socket = Socket()
        try {
            socket!!.bind(InetSocketAddress(port))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun startReadingStream() {
        val readAsyncTask = ReadAsyncTask(this)
        readAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun emit(MessagePacket: MessagePacket?) {
        val writeAsyncTask = WriteAsyncTask(socket!!)
        writeAsyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, MessagePacket)
    }

    fun onDestroy() {
        val builder = MessagePacketBuilder()
        builder.setEvent("disconnect")
        builder.setType("message")
        builder.setMessage("Disconnect")
        val messagePacket = builder.build()
        emit(messagePacket)
        if (socket != null) {
            try {
                socket!!.close()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }
}