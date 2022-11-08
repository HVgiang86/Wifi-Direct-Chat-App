package com.example.wifidirectchatapp.model

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.wifidirectchatapp.activity.ChatActivity
import com.example.wifidirectchatapp.adapter.MessageAdapter

class MessageManager private constructor() {
    private val messageList: MutableList<Message>
    private var activity: ChatActivity? = null
    private var rv: RecyclerView? = null
    private var adapter: MessageAdapter? = null

    init {
        messageList = ArrayList()
        messageList.add(Message("Welcome back!", isServer = false, isFile = false))
    }

    private object Holder {
        val INSTANCE = MessageManager()
    }

    companion object {
        fun getInstance(): MessageManager {
            return Holder.INSTANCE
        }
    }

    fun setActivity(activity: ChatActivity?) {
        this.activity = activity
    }

    fun setRv(rv: RecyclerView?) {
        this.rv = rv
    }

    fun addMessage(message: Message) {
        messageList.add(message)
        if (adapter != null) {
            activity!!.runOnUiThread {
                Log.d("Server log", "Message List update")
                adapter!!.notifyItemInserted(messageList.size - 1)
                rv!!.scrollToPosition(messageList.size - 1)
            }
        }
    }

    fun getMessageList(): List<Message> {
        return messageList
    }

    fun setAdapter(adapter: MessageAdapter?) {
        this.adapter = adapter
    }

}