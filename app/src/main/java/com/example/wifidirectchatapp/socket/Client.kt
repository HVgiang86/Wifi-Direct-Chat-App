package com.example.wifidirectchatapp.socket

class Client private constructor() {
    init {

    }

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
