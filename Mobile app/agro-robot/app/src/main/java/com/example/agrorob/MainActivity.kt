package com.example.agrorob

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothStatusTextView: TextView
    private lateinit var bluetoothStatusIcon: ImageView
    private lateinit var bluetoothOnButton: Button
    private lateinit var bluetoothDiscoverableButton: Button
    private lateinit var bluetoothDevices: Button
    private var bAdapter: BluetoothAdapter? = null
    private val PERMISSIONS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the TextView and ImageView references
        bluetoothStatusTextView = findViewById(R.id.bluetoothStatus)
        bluetoothStatusIcon = findViewById(R.id.bluetoothImage)
        bluetoothOnButton = findViewById(R.id.bluetoothOn)
        bluetoothDevices = findViewById(R.id.bluetoothPairedDevices)
        bluetoothDiscoverableButton = findViewById(R.id.bluetoothDiscoverable)

        checkSelfPermission("BLUETOOTH")
        checkSelfPermission("BLUETOOTH_ADMIN")
        checkSelfPermission("BLUETOOTH_CONNECT")
        checkSelfPermission("BLUETOOTH_SCAN")

        checkPermissions {
            // Get the BluetoothAdapter
            val bluetoothManager = getSystemService(BluetoothManager::class.java)
            bAdapter = bluetoothManager.adapter
            if (bAdapter == null) {
                // Device does not support Bluetooth
                bluetoothStatusTextView.text = getString(R.string.bluetooth_not_available)
                bluetoothOnButton.isEnabled = false
            } else {
                // Bluetooth is available
                bluetoothStatusTextView.text = getString(R.string.bluetooth_available)
            }
            if (bAdapter?.isEnabled == true) {
                bluetoothStatusIcon.setImageResource(R.drawable.ic_bluetooth_on)
                bluetoothOnButton.isEnabled = false
            } else {
                bluetoothStatusIcon.setImageResource(R.drawable.ic_bluetooth_off)
            }

            bluetoothOnButton.setOnClickListener {
                if (bAdapter?.isEnabled == true) {
                    Toast.makeText(this, "Already on", Toast.LENGTH_LONG).show()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestMultiplePermissions.launch(arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT))
                    }
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    requestBluetooth.launch(enableBtIntent)
                }
            }
            bluetoothDiscoverableButton.setOnClickListener {
                if (bAdapter?.isDiscovering == false) {
                    Toast.makeText(this, "Making your device discoverable", Toast.LENGTH_LONG).show()
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                    startActivityForResult(intent, REQUEST_ENABLE_DISCOVERY)
                }
            }
            bluetoothDevices.setOnClickListener {
                val intent = Intent(this, BluetoothDevicesActivity::class.java)
                startActivity(intent)
            }
        }
    }
    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_ENABLE_DISCOVERY = 2
    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth was enabled successfully
            bluetoothStatusIcon.setImageResource(R.drawable.ic_bluetooth_on)
            Toast.makeText(this, "Bluetooth enabled successfully", Toast.LENGTH_SHORT).show()
            bluetoothOnButton.isEnabled = false
        }else{
            // Bluetooth enabling failed or was denied by the user
            Toast.makeText(this, "Bluetooth enabling failed or was denied", Toast.LENGTH_SHORT).show()
            bluetoothOnButton.isEnabled = true
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }


    private fun checkPermissions(callback: () -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val missingPermissions = permissions.filter {
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                requestPermissions(missingPermissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)
            } else {
                callback()
            }
        } else {
            callback()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var allGranted = true

            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    // Handle the permission denied case for each permission
                }
            }

            if (allGranted) {
                // Permissions granted, do your Bluetooth work here
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                // Permissions granted, refresh the view
                recreate()
            } else {
                // Handle the case where one or more permissions were denied
                Toast.makeText(this, "The required permissions were not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}