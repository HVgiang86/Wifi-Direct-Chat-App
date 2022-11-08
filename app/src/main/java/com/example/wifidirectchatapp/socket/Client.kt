package com.example.wifidirectchatapp.socket

class Client private constructor() {
    val socket : MySocket = MySocket()

    private object Holder {
        val INSTANCE = Client()
    }

    companion object{
        @JvmStatic
        fun getInstance() : Client {
            return Holder.INSTANCE
        }
    }


}
