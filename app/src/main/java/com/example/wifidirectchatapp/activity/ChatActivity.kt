package com.example.wifidirectchatapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.wifidirectchatapp.R
import com.example.wifidirectchatapp.model.MessageManager

class ChatActivity : AppCompatActivity() {
    companion object{
        const val CLIENT_SOCKET_MODE = "CLIENT_SOCKET_MODE"
        const val SERVER_SOCKET_MODE = "SERVER_SOCKET_MODE"
        const val SOCKET_MODE_EXTRA = "SOCKET_MODE_EXTRA"
        const val IP_SOCKET_EXTRA = "IP_SOCKET_EXTRA"
        const val BUNDLE_KEY = "SOCKET_INFO_BUNDLE_KEY"
        const val SOCKET_PORT = 5000
        const val TAG = "CHAT ACTIVITY LOG"
    }

    private lateinit var ip: String
    private lateinit var socketModeTv : TextView
    private lateinit var messageEdt : EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageManager: MessageManager
    private lateinit var attachFileBtn : ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        socketModeTv = findViewById(R.id.socket_mode_tv)
        messageEdt = findViewById(R.id.message_edt)
        recyclerView = findViewById(R.id.recycler_view)
        attachFileBtn = findViewById(R.id.attach_file_btn)
        messageManager = MessageManager.getInstance()

        val bundle = intent.getBundleExtra(BUNDLE_KEY)
        if (bundle != null) {
            ip = bundle.getString(IP_SOCKET_EXTRA,"")
            socketModeTv.text = bundle.getString(SOCKET_MODE_EXTRA,"fail")
        }

        Log.d(TAG, "IP: ${ip}; Socket mode: ${socketModeTv.text}")
    }
}