package com.example.wifidirectchatapp.activity

import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.wifidirectchatapp.R
import com.example.wifidirectchatapp.adapter.MessageAdapter
import com.example.wifidirectchatapp.model.Message
import com.example.wifidirectchatapp.model.MessageManager
import com.example.wifidirectchatapp.socket.Client
import com.example.wifidirectchatapp.socket.OnNewMessageListener
import com.example.wifidirectchatapp.socket.Server
import com.example.wifidirectchatapp.utilities.FilePathGetter
import java.io.File


class ChatActivity : AppCompatActivity() {
    companion object {
        const val CLIENT_SOCKET_MODE = "CLIENT_SOCKET_MODE"
        const val SERVER_SOCKET_MODE = "SERVER_SOCKET_MODE"
        const val SOCKET_MODE_EXTRA = "SOCKET_MODE_EXTRA"
        const val IP_SOCKET_EXTRA = "IP_SOCKET_EXTRA"
        const val BUNDLE_KEY = "SOCKET_INFO_BUNDLE_KEY"
        const val SOCKET_PORT = 5000
        const val TAG = "CHAT ACTIVITY LOG"
        const val MAX_FILE_SIZE = 50*1024*1024
    }

    private lateinit var ip: String
    private lateinit var socketModeTv: TextView
    private lateinit var messageEdt: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageManager: MessageManager
    private lateinit var attachFileBtn: ImageButton
    private lateinit var sendMessageBtn: Button

    private var isServer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //ask for external storage permission
        if (shouldAskPermissions()) {
            askPermissions()
        }

        socketModeTv = findViewById(R.id.socket_mode_tv)
        messageEdt = findViewById(R.id.message_edt)
        recyclerView = findViewById(R.id.recycler_view)
        attachFileBtn = findViewById(R.id.attach_file_btn)
        sendMessageBtn = findViewById(R.id.send_message_btn)
        messageManager = MessageManager.getInstance()

        val bundle = intent.getBundleExtra(BUNDLE_KEY)
        if (bundle != null) {
            ip = bundle.getString(IP_SOCKET_EXTRA, "")
            val socketModeStr = bundle.getString(SOCKET_MODE_EXTRA, "fail")
            socketModeTv.text = socketModeStr
            isServer = socketModeStr.equals(SERVER_SOCKET_MODE)
        }
        Log.d(TAG, "IP: ${ip}; Socket mode: ${socketModeTv.text}")

        attachFileBtn.setOnClickListener { chooseFile() }
        sendMessageBtn.setOnClickListener { sendMessage() }

        if (isServer)
            createServerSocket()
        else
            openClientSocket()

        displayMessageList()

    }

    private fun openClientSocket() {
        Log.d(TAG, "Connecting to socket server: ip: ${ip}; port: $SOCKET_PORT")
        val socket = Client.getInstance().socket
        socket.createSocket(SOCKET_PORT)
        socket.connectSocket(ip, SOCKET_PORT.toString())

        socket.newMessageListener = OnNewMessageListener()
    }

    private fun createServerSocket(){
        Server.getInstance().createServer(SOCKET_PORT)
    }

    private fun displayMessageList() {
        messageManager = MessageManager.getInstance()
        val messageList = messageManager.getMessageList()
        val messageAdapter = MessageAdapter(messageList, this)

        messageManager.setActivity(this)
        recyclerView.adapter = messageAdapter
        messageManager.setRv(recyclerView)
        messageManager.setAdapter(messageAdapter)

    }

    private fun sendMessage() {
        val s = messageEdt.text.toString().trim()
        if (s.isEmpty()) return

        if(isServer)
            Server.getInstance().connectedSocket?.emitMessage(s)
        else
            Client.getInstance().socket.emitMessage(s)

        messageManager.addMessage(Message(s, isServer = true, isFile = false))
        messageEdt.setText("")
    }

    private fun chooseFile() {
        var chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooseFileIntent.type = "*/*"
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file")
        startActivityForResult(chooseFileIntent, 2000)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        openFileChooser(data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openFileChooser(dataIntent: Intent?) {
        var src = ""
        if (dataIntent != null) {
            val fileUri: Uri = dataIntent.data!!
            Log.i("FILE CHOSEN LOG", "Uri: $fileUri")
            try {
                src = FilePathGetter.getPath(this, fileUri).toString()
                Log.i("FILE CHOSEN LOG", "Uri: $src")
            } catch (e: Exception) {
                Log.e("FILE CHOSEN LOG", "Error: $e")
                Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
            }
        }

        val filePath = src
        val lastIndex = src.lastIndexOf("/")
        val filename = src.substring(lastIndex + 1)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Send file?")
            .setMessage("Do you want to send local file?\nFile path: $src")
        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
            val file = File(filePath, filename)
            if (file.length() > MAX_FILE_SIZE) {
                Toast.makeText(this, "File must be lower than 50MB", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (isServer)
                Server.getInstance().connectedSocket?.emitFile(filePath, filename)
            else
                Client.getInstance().socket.emitFile(filePath,filename)

            messageManager.addMessage(Message(filePath, isServer = true, isFile = true))
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.create().show()
    }

    override fun onDestroy() {
        if(isServer)
            Server.getInstance().onDestroy()
        else
            Client.getInstance().socket.onDestroy()
        super.onDestroy()
    }

    //request external memory permission
    private fun shouldAskPermissions(): Boolean {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
    }

    @TargetApi(23)
    private fun askPermissions() {
        val permissions = arrayOf(
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
        val requestCode = 200
        requestPermissions(permissions, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}