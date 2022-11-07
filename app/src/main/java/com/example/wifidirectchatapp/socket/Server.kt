package com.example.wifidirectchatapp.socket

import android.util.Log
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacketBuilder
import com.example.wifidirectchatapp.my_socket_library.IO
import com.example.wifidirectchatapp.my_socket_library.SingleSocket
import java.io.IOException
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.SocketException

class Server  //Singleton
private constructor() {
    private var connectedSocket: MySocket? = null
    private var hasClientConnected = false
    private var serverSocket: ServerSocket? = null
    fun createServer(port: Int) {
        val socketServerThread = Thread(SocketServerThread(port))
        socketServerThread.start()
    }

    fun hasClientConnected(): Boolean {
        return hasClientConnected
    }

    fun onDestroy() {
        val builder = MessagePacketBuilder()
        builder.setEvent(IO.CLIENT_DISCONNECT)
        builder.setEvent(IO.SEND_MESSAGE)
        builder.setMessage("Disconnect")
        connectedSocket!!.emit(builder.build())
        if (serverSocket != null) {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }

    private inner class SocketServerThread(private val port: Int) : Thread() {
        override fun run() {
            try {
                Log.d(TAG, "CURRENT IP: $ipAddress")
                // create ServerSocket using specified port
                serverSocket = ServerSocket()
                serverSocket!!.bind(InetSocketAddress(port))
                while (true) {
                    // block the call until connection is created and return
                    // Socket object
                    val socket = serverSocket!!.accept()
                    hasClientConnected = true
                    if (connectedSocket!!.socket !== socket) {
                        connectedSocket = MySocket()
                        connectedSocket!!.socket = socket
                    }
                    Log.d(
                        TAG, "Client accepted: " + " Socket IP: " + socket.inetAddress
                                + " Port: " + socket.port
                    )
                    connectedSocket!!.newMessageListener = OnNewMessageListener()
                    connectedSocket!!.onDisconnectListener = object : IO.OnDisconnectListener {
                        override fun onDisconnect(socket: SingleSocket?) {
                            socket?.onDestroy()
                        }
                    }
                    connectedSocket!!.startReadingStream()
                }
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }

    companion object {
        val instance = Server()
        private const val TAG = "SERVER TAG"

        // TODO Auto-generated catch block
        val ipAddress: String
            get() {
                var ip = ""
                try {
                    val enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces()
                    while (enumNetworkInterfaces.hasMoreElements()) {
                        val networkInterface = enumNetworkInterfaces.nextElement()
                        val enumInetAddress = networkInterface.inetAddresses
                        while (enumInetAddress.hasMoreElements()) {
                            val inetAddress = enumInetAddress.nextElement()
                            if (inetAddress.isSiteLocalAddress) {
                                ip = ip + "Server running at : " + inetAddress.hostAddress
                            }
                        }
                    }
                } catch (e: SocketException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                    ip += "Something Wrong! $e\n"
                }
                if (ip.length > 47) {
                    val i = ip.indexOf("Server", 5)
                    ip = ipAddress.substring(0, i)
                }
                return ip
            }
    }
}