package com.example.wifidirectchatapp.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.example.wifidirectchatapp.R
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.wifidirectchatapp.model.Message
import com.example.wifidirectchatapp.utilities.FileOpener
import java.io.File

class MessageAdapter(private val messageList: List<Message>, private val context: Context) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = when (viewType) {
            SERVER_MESSAGE_TYPE -> inflater.inflate(
                R.layout.server_message_item,
                parent,
                false
            )
            CLIENT_MESSAGE_TYPE -> inflater.inflate(
                R.layout.client_message_item,
                parent,
                false
            )
            SERVER_FILE_TYPE -> inflater.inflate(
                R.layout.server_file_item,
                parent,
                false
            )
            else -> inflater.inflate(R.layout.client_file_item, parent, false)
        }
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        Log.d("Adapter log", "Item: Message: " + message.content)
        val content = message.content?.trim { it <= ' ' }
        if (getItemViewType(position) == CLIENT_MESSAGE_TYPE || getItemViewType(position) == SERVER_MESSAGE_TYPE) holder.contentTv.text =
            content
        if (getItemViewType(position) == SERVER_FILE_TYPE || getItemViewType(position) == CLIENT_FILE_TYPE) {
            val i = content?.lastIndexOf("/")
            val filename = content?.substring(i!! + 1)
            holder.contentTv.text = filename
            holder.contentTv.setOnClickListener {
                showFileChooser(content.toString())
                Log.d("Adapter log", "FILE CLICKED!")
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].isServer) {
            if (messageList[position].isFile) SERVER_FILE_TYPE else SERVER_MESSAGE_TYPE
        } else {
            if (messageList[position].isFile) CLIENT_FILE_TYPE else CLIENT_MESSAGE_TYPE
        }
    }

    private fun showFileChooser(filePath: String) {
        val file = File(filePath)
        FileOpener.openFile(context,file)

//        val extension = filePath.substring(filePath.lastIndexOf(".") + 1)
//        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
//        val baseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(filePath))
//        baseIntent.type = type
//        val i = filePath.lastIndexOf("/")
//        val selectedUri = Uri.parse(filePath.substring(0, i))
//        Log.d("Adapter Log", "test path: $selectedUri")
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.setDataAndType(selectedUri, "*/*")
//        if (intent.resolveActivityInfo(context.packageManager, 0) != null) {
//            context.startActivity(intent)
//        } else {
//            // if you reach this place, it means there is no any file
//            // explorer app installed on your device
//            Toast.makeText(context.applicationContext, "File unable to open", Toast.LENGTH_LONG)
//                .show()
//        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contentTv: TextView

        init {
            contentTv = itemView.findViewById(R.id.message_content)
        }
    }

    companion object {
        private const val SERVER_MESSAGE_TYPE = 1
        private const val SERVER_FILE_TYPE = 3
        private const val CLIENT_FILE_TYPE = 4
        private const val CLIENT_MESSAGE_TYPE = 2
    }
}