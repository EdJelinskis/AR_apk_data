package com.example.agrorob

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.agrorob.ui.start.StartViewModel
import com.example.agrorob.adapters.BluetoothConnection
import java.io.IOException

class RoboMainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bluetoothDevice: BluetoothDevice

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_robo_main)

        val _bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>("KEY_BLUETOOTH_DEVICE")
        if (_bluetoothDevice != null) {
            bluetoothDevice = _bluetoothDevice
            Log.d("test006", "${_bluetoothDevice.name} = ${_bluetoothDevice.address}")
            val sharedViewModel: StartViewModel by viewModels()
            sharedViewModel.setBluetoothDevice(_bluetoothDevice)
        } else {
            Log.d("test006", "No device passed")
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_robo_main) as NavHostFragment
        navController = navHostFragment.navController

        // Set up the BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.setupWithNavController(navController)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothConnection.socket?.let { socket ->
            if (socket.isConnected) {
                try {
                    socket.close()
                } catch (e: IOException) {
                    Log.e("onDestroy", "Error closing Bluetooth socket: ${e.message}")
                }
            }
        }
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
}