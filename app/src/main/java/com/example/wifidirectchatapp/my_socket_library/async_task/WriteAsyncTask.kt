@file:Suppress("DEPRECATION")

package com.example.wifidirectchatapp.my_socket_library.async_task

import android.os.AsyncTask
import android.util.Log
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

class WriteAsyncTask(private val mSocket: Socket) : AsyncTask<MessagePacket?, Void?, Void?>() {
    companion object {
        private const val TAG = "Transferring Log"
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: MessagePacket?): Void? {
        //Here we send an message package to server
        //Message package's content will be sent in the following order: event -> sender -> fileSize (if file)
        // -> message -> data in byte array (if file), each properties will be sent on a new line

        //message package to send
        val messagePacket = params[0]
        try {
            val os = mSocket.getOutputStream()
            val dos = DataOutputStream(os)
            dos.flush()

            //write event
            dos.writeBytes(
                """
                ${messagePacket?.event}
                """.trimIndent()
            )

            if (messagePacket!!.isFile) {
                //write data size in byte
                dos.writeBytes(
                    """
                    ${messagePacket.dataSizeInByte}
                    """.trimIndent()
                )

                //write file name
                dos.writeBytes(
                    """
                    ${messagePacket.message}
                    
                    """.trimIndent()
                )

                //write data in byte array
                val data = messagePacket.data
                os.write(data, 0, messagePacket.dataSizeInByte)
            } else {
                //write message
                dos.writeUTF(
                    """
                    ${messagePacket.message}
                    
                    """.trimIndent()
                )
                //                dos.writeBytes(MessagePacket.getMessage() + "\n");
            }
            Log.d(TAG, "transferred")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "Data transferred fail!")
        }
        return null
    }

}