package com.example.agrorob

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.agrorob.adapters.BluetoothDeviceAdapter
import com.example.agrorob.adapters.ConnectThread

class BluetoothDevicesActivity : AppCompatActivity() {
    private lateinit var devicesRecyclerView: RecyclerView
    private lateinit var loadingDevices: ProgressBar
    private lateinit var bAdapter: BluetoothAdapter
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    private lateinit var connectThread: ConnectThread
    private val devices = mutableListOf<BluetoothDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_devices)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        devicesRecyclerView = findViewById(R.id.deviceList)
        loadingDevices = findViewById(R.id.loadingDevices)
        deviceAdapter = BluetoothDeviceAdapter(devices) { device ->
            // Attempt to connect to the selected device
            // Implement your own Bluetooth connection logic here
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(this, "The required permissions were not granted", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(this, "Connecting to ${device.name}", Toast.LENGTH_SHORT).show()
            devicesRecyclerView.isVisible = false
            loadingDevices.isVisible = true
            connectThread = ConnectThread(this, device)
            connectThread.connect()
        }

        loadingDevices.isVisible = false
        devicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BluetoothDevicesActivity)
            adapter = deviceAdapter
        }

        bAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun disableDevicesRecyclerView(durationMillis: Long) {
        devicesRecyclerView.isVisible = false
        loadingDevices.isVisible = true

        Handler(Looper.getMainLooper()).postDelayed({
            loadingDevices.isVisible = false
            devicesRecyclerView.isVisible = true
        }, durationMillis)
    }

    override fun onResume() {
        super.onResume()
        if (::connectThread.isInitialized) {
            connectThread.disconnect()
            disableDevicesRecyclerView(3000)
        }
    }

    private fun startDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            return
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(deviceAdapter.discoveryReceiver, filter)

        bAdapter.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::connectThread.isInitialized) {
            connectThread.disconnect()
        }
        if (::deviceAdapter.isInitialized) {
            try {
                unregisterReceiver(deviceAdapter.discoveryReceiver)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered, ignore the exception
            }
        }
    }
}