@file:Suppress("DEPRECATION")

package com.example.wifidirectchatapp.my_socket_library.async_task

import android.os.AsyncTask
import android.util.Log
import com.example.wifidirectchatapp.my_socket_library.SingleSocket
import java.io.IOException
import java.net.InetSocketAddress

class SocketConnectAsyncTask(private val mSocket: SingleSocket) :
    AsyncTask<String?, Void?, Void?>() {

    companion object {
        private const val TAG = "SOCKET CONNECT LOG"
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: String?): Void? {
        val ip = params[0]
        val portStr = params[1]
        val port = portStr?.toInt()
        try {
            mSocket.socket!!.connect(InetSocketAddress(ip, port!!), 5000)
            Log.d(
                TAG,
                "Connected to server! ip: " + mSocket.socket!!.inetAddress + " port: " + mSocket.socket!!.port
            )
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "Connected fail!")
        }
        return null
    }

}