package com.example.wifidirectchatapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.wifidirectchatapp.R
import com.example.wifidirectchatapp.broadcast_receiver.WifiP2pReceiver
import com.example.wifidirectchatapp.socket.Server

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 1000

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiToggleBtn: Button
    private lateinit var peerDiscoveryBtn: Button
    lateinit var connectionStateTv: TextView
    private lateinit var peersListView: ListView

    private lateinit var receiver: WifiP2pReceiver

    private val TAG = "P2P TAG"

    private lateinit var peers: MutableList<WifiP2pDevice>
    private var deviceName = Array(5) { "" }
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiToggleBtn = findViewById(R.id.wifi_toggle_btn)
        peerDiscoveryBtn = findViewById(R.id.device_discovery_btn)
        connectionStateTv = findViewById(R.id.connection_status_tv)
        peersListView = findViewById(R.id.device_list)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (wifiManager.isWifiEnabled) {
            Log.d(TAG, "Wifi enabled!")
            connectionStateTv.text = getString(R.string.connected)
            wifiToggleBtn.text = getString(R.string.turn_wifi_off)
        } else {
            Log.d(TAG, "Wifi disabled!")
            connectionStateTv.text = getString(R.string.disconnected)
            wifiToggleBtn.text = getString(R.string.turn_wifi_on)
        }

        wifiToggleBtn.setOnClickListener { changeWifiState() }
        peerDiscoveryBtn.setOnClickListener { startPeersDiscovery() }

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        receiver = WifiP2pReceiver(manager, channel, this)
        registryBroadcastReceiver()

        peers = mutableListOf()

        adapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_list_item_1, deviceName
        )
        peersListView.adapter = adapter
        peersListView.onItemClickListener = onItemClickListener
    }

    private fun changeWifiState() {
        if (wifiToggleBtn.text.equals(getString(R.string.turn_wifi_off))) {
            wifiManager.isWifiEnabled = false
            wifiToggleBtn.text = getString(R.string.turn_wifi_on)
        } else {
            wifiManager.isWifiEnabled = true
            wifiToggleBtn.text = getString(R.string.turn_wifi_off)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun permissionCheck() {

        val permission = arrayOf(
            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permission, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        baseContext.unregisterReceiver(receiver)
        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                //discovery started successfully
            }

            override fun onFailure(p0: Int) {
                //discovery not started
            }
        })
    }

    private fun registryBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        baseContext.registerReceiver(receiver, intentFilter)
    }

    @SuppressLint("MissingPermission")
    private fun startPeersDiscovery() {
        permissionCheck()
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                //discovery started successfully
                connectionStateTv.text = "Peers discovery started!"
            }

            override fun onFailure(p0: Int) {
                //discovery not started
                connectionStateTv.text =
                    "Peers discovery starting failed, pls check the connection!"
            }
        })
    }

    val peerListListener = WifiP2pManager.PeerListListener { p0 ->
        val peersList = p0!!.deviceList
        if (peersList.isEmpty()) {
            Log.d(TAG, "No device found")
        } else {
            Log.d(TAG, "${peersList.size} devices found!")
        }

        if (!peersList.equals(peers)) {
            peers.clear()
            peers.addAll(peersList)

            if (peers.size == 0) Toast.makeText(
                this, "No device found!", Toast.LENGTH_SHORT
            ).show()
            else {
                var i = 0
                peers.forEach { d ->
                    deviceName[i++] = d.deviceName
                    Log.d(TAG, "Device name: " + d.deviceName)
                }
                adapter.notifyDataSetChanged()

            }
        }
    }

    @SuppressLint("MissingPermission")
    val onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        val device = peers[position]
        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(
                    applicationContext,
                    "Connected to " + device.deviceName,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(p0: Int) {
                Toast.makeText(applicationContext, "not connected!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { wifiP2pInfo ->
        Log.d(TAG, "Connection Info Listener")
        val groupOwnerAddress = wifiP2pInfo.groupOwnerAddress

        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()

        val isHost: Boolean = (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)

        if (isHost) {
            connectionStateTv.text = "HOST"
            bundle.putString(ChatActivity.SOCKET_MODE_EXTRA,ChatActivity.SERVER_SOCKET_MODE)
        }
        else {
            connectionStateTv.text = "CLIENT"
            bundle.putString(ChatActivity.SOCKET_MODE_EXTRA,ChatActivity.CLIENT_SOCKET_MODE)
        }

        Log.d(TAG,"My ip: ${Server.ipAddress}")

        val ip : String = groupOwnerAddress.hostAddress as String
        if (ip.isNotEmpty()) {
            Log.d(TAG, "Group owner address: ${groupOwnerAddress.hostAddress}")
            bundle.putString(ChatActivity.IP_SOCKET_EXTRA,ip)
        }

        intent.putExtra(ChatActivity.BUNDLE_KEY,bundle)
        startActivity(intent)
    }
}