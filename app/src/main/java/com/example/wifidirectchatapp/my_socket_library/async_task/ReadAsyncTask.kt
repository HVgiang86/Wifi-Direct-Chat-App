/**
 * This class is an Async Task that has responsibility to transfer data to Socket
 * @author Hoang Giang
 * @constructor a SingleSocket object is param for constructor, that's socket we will use
 */


@file:Suppress("DEPRECATION")
package com.example.wifidirectchatapp.my_socket_library.async_task

import android.os.AsyncTask
import android.util.Log
import com.example.wifidirectchatapp.my_socket_library.IO
import com.example.wifidirectchatapp.my_socket_library.SingleSocket
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacketBuilder
import java.io.DataInputStream
import java.io.IOException

class ReadAsyncTask(private val socket: SingleSocket) : AsyncTask<Void?, Void?, Void?>() {
    companion object {
        private const val TAG = "Data Received Tag"
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): Void? {
        //Here we looking for incoming data from socket
        //Every time has data on input stream, we will read it until end of stream
        //Incoming message package structured in the following order: event -> sender -> fileSize (if file)
        // -> message -> data in byte array (if file)
        //kep running this task until an DISCONNECT event was sent to
        try {
            while (true) {
                val `is` = socket.socket?.getInputStream()
                val dis = DataInputStream(`is`)
                Log.d(TAG, "Read Async Task Running")
                Log.d(TAG, "New Data")
                val builder = MessagePacketBuilder()

                //read event
                val event = dis.readLine()
                builder.setEvent(event)

                Log.d(TAG, "Event: $event")

                if (event.equals(IO.SEND_FILE, ignoreCase = true)) {
                    builder.setType(IO.SEND_FILE)

                    //read file size
                    val fileSize = dis.readLine().toInt()
                    Log.d(TAG, "file size: $fileSize")

                    //read filename as message properties
                    val filename = dis.readLine()
                    Log.d(TAG, "Filename: $filename")

                    //read data of file in byte array
                    val bytes = ByteArray(fileSize)
                    //dis.read(bytes);
                    dis.readFully(bytes)
                    Log.d(TAG, "byte array done!")
                    builder.setMessage(filename)
                    builder.setData(bytes)
                    builder.setDataSizeInByte(fileSize)
                } else {
                    builder.setType(IO.SEND_MESSAGE)
                    //read message
                    val message = dis.readUTF()
                    //String message = br.readLine();
                    builder.setMessage(message)
                }
                val messagePacket = builder.build()
                Log.d(TAG, "Data received: $messagePacket")

                //if meet disconnect event, finish read task
                if (messagePacket.event.equals(IO.CLIENT_DISCONNECT, ignoreCase = true)) {
                    socket.onDisconnectListener?.onDisconnect(socket)
                    onDestroy()
                    return null
                }
                socket.newMessageListener?.onNewMessage(socket, messagePacket)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    private fun onDestroy() {
        try {
            socket.socket?.close()
            socket.onDisconnectListener?.onDisconnect(socket)
            Log.d("SERVER TAG", "Client disconnected!")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}