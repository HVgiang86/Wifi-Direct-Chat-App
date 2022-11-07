package com.example.wifidirectchatapp.utilities

import android.R
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.wifidirectchatapp.utilities.model.PeersList

class WifiP2pHelper private constructor(){

    companion object {
        val peers = mutableListOf<WifiP2pDevice>()
        val peerListListener = WifiP2pManager.PeerListListener { p0 ->
            var peersList = p0!!.deviceList
            if (!peersList.equals(peers)) {
                peers.clear()
                peers.addAll(peersList)

                if (peers.size == 0)
                    Toast.makeText(this, "No device found!", Toast.LENGTH_SHORT).show()
                else
                {
                    var i = 0
                    peers.forEach {d -> deviceName[i++] = d.deviceName
                        Log.d(TAG,"Device name: " + d.deviceName)}
                    var adapter = ArrayAdapter(applicationContext, R.layout.simple_list_item_1, deviceName)
                    peersListView.adapter = adapter
                }
            }
        }
    }


}