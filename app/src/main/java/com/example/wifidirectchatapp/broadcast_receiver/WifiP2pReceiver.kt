package com.example.wifidirectchatapp.broadcast_receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import com.example.wifidirectchatapp.activity.MainActivity

class WifiP2pReceiver(
    private val mManager: WifiP2pManager,
    private val mChannel: WifiP2pManager.Channel,
    private val mainActivity: MainActivity
) : BroadcastReceiver() {


    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                    Toast.makeText(context, "Wifi direct is ON",Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(context, "Wifi direct is OFF", Toast.LENGTH_SHORT).show()
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                mManager.requestPeers(mChannel, mainActivity.peerListListener)
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {

            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {}

            
        }
    }
}