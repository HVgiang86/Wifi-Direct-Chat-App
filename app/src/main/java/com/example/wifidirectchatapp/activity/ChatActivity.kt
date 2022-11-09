package com.example.wifidirectchatapp.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.wifidirectchatapp.R
import com.example.wifidirectchatapp.adapter.MessageAdapter
import com.example.wifidirectchatapp.model.Message
import com.example.wifidirectchatapp.model.MessageManager
import com.example.wifidirectchatapp.socket.Client
import com.example.wifidirectchatapp.socket.OnNewMessageListener
import com.example.wifidirectchatapp.socket.Server
import com.example.wifidirectchatapp.utilities.FilePathGetter
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.File


class ChatActivity : AppCompatActivity() {
    //Constant Value used to be KEY and MARK_VALUE
    companion object {
        const val CLIENT_SOCKET_MODE = "CLIENT_SOCKET_MODE"
        const val SERVER_SOCKET_MODE = "SERVER_SOCKET_MODE"
        const val SOCKET_MODE_EXTRA = "SOCKET_MODE_EXTRA"
        const val IP_SOCKET_EXTRA = "IP_SOCKET_EXTRA"
        const val BUNDLE_KEY = "SOCKET_INFO_BUNDLE_KEY"
        const val SOCKET_PORT = 5000
        const val TAG = "CHAT ACTIVITY LOG"
        const val MAX_FILE_SIZE = 50 * 1024 * 1024
    }

    private lateinit var socketIp: String
    private lateinit var messageManager: MessageManager
    private var isServer: Boolean = false

    private lateinit var mManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //ask for external storage permission
        if (shouldAskPermissions()) {
            askPermissions()
        }

        messageManager = MessageManager.getInstance()
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager.initialize(this, mainLooper,null)

        //get socket mode, host's ip from activity's launch intent
        val bundle = intent.getBundleExtra(BUNDLE_KEY)
        if (bundle != null) {
            socketIp = bundle.getString(IP_SOCKET_EXTRA, "")
            val socketModeStr = bundle.getString(SOCKET_MODE_EXTRA, "fail")
            socket_mode_tv.text = socketModeStr
            isServer = socketModeStr.equals(SERVER_SOCKET_MODE)
        }
        Log.d(TAG, "IP: ${socketIp}; Socket mode: ${socket_mode_tv.text}")

        attach_file_btn.setOnClickListener { chooseFile() }
        send_message_btn.setOnClickListener { sendMessage() }

        if (isServer)
            createServerSocket()
        else
            connectClientToSocketServer()

        initDisplayingMessageList()
    }

    override fun onDestroy() {
        if (isServer)
            Server.getInstance().onDestroy()
        else
            Client.getInstance().socket.onDestroy()
        disconnectWifiDirect()
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    fun disconnectWifiDirect() {
        mManager.requestGroupInfo(mChannel
        ) { group ->
            if (group != null) {
                mManager.removeGroup(mChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d(TAG, "removeGroup onSuccess -")
                    }

                    override fun onFailure(reason: Int) {
                        Log.d(TAG, "removeGroup onFailure -$reason")
                    }
                })
            }
        }
    }

    private fun connectClientToSocketServer() {
        Log.d(TAG, "Connecting to socket server: ip: ${socketIp}; port: $SOCKET_PORT")
        val socket = Client.getInstance().socket
        socket.createSocket(SOCKET_PORT)
        socket.connectSocket(socketIp, SOCKET_PORT.toString())

        socket.newMessageListener = OnNewMessageListener()
    }

    private fun createServerSocket() {
        Server.getInstance().createServer(SOCKET_PORT)
    }

    /**
     * This function used to display list of message.
     * Messages get from Message Manager's list
     */
    private fun initDisplayingMessageList() {
        messageManager = MessageManager.getInstance()
        val messageList = messageManager.getMessageList()
        val messageAdapter = MessageAdapter(messageList, this)

        messageManager.setActivity(this)
        recycler_view.adapter = messageAdapter
        messageManager.setRv(recycler_view)
        messageManager.setAdapter(messageAdapter)

    }

    private fun sendMessage() {
        val messageContent = message_edt.text.toString().trim()
        if (messageContent.isEmpty()) return

        if (isServer)
            Server.getInstance().connectedSocket?.emitMessage(messageContent)
        else
            Client.getInstance().socket.emitMessage(messageContent)

        messageManager.addMessage(Message(messageContent, isServer = true, isFile = false))
        message_edt.setText("")
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
                Client.getInstance().socket.emitFile(filePath, filename)

            messageManager.addMessage(Message(filePath, isServer = true, isFile = true))
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.create().show()
    }

    //request external memory permission
    private fun shouldAskPermissions(): Boolean {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
    }

    /**
     * This function request for important permissions, if user accept, application will work correctly
     */
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