package com.example.agrorob.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import com.example.agrorob.BluetoothDevicesActivity
import com.example.agrorob.RoboMainActivity
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.reflect.Method
import java.util.*

@SuppressLint("MissingPermission")
class ConnectThread(private val context: Context, private val device: BluetoothDevice) {

    private var mmSocket: BluetoothSocket? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun connect() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Start SDP discovery process to retrieve the UUIDs of the remote device's services
                device.fetchUuidsWithSdp()
                Thread.sleep(1000) // Wait for SDP discovery to complete
                val uuids: Array<out ParcelUuid>? = device.uuids // Get the UUIDs of the remote device's services

                // Try connecting to each service until a connection is successful
                try {
                    val m: Method = device.javaClass.getMethod(
                        "createInsecureRfcommSocket", *arrayOf<Class<*>?>(
                            Int::class.javaPrimitiveType
                        )
                    )
                    mmSocket = m.invoke(device, 1) as BluetoothSocket
                    mmSocket?.connect()
                } catch (e: IOException) {
                    Log.e(BluetoothDeviceSocket.TAG, "Unable to connect")
                    // Connection failed, try next UUID
                }

                if (mmSocket?.isConnected == true) {
                    // Connection successful, start your next activity here
                    BluetoothConnection.socket = mmSocket
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, RoboMainActivity::class.java)
                        intent.putExtra("KEY_BLUETOOTH_DEVICE", device)
                        context.startActivity(intent)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        // Unable to connect to any of the device's services, handle this case
                        Toast.makeText(context, "Unable to connect to ${device.name}", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(context, RoboMainActivity::class.java)
//                        intent.putExtra("KEY_BLUETOOTH_DEVICE", device)
//                        context.startActivity(intent)
                        (context as BluetoothDevicesActivity).finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Error occurred, handle exception
                    Toast.makeText(context, "Error connecting to ${device.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun disconnect() {
        try {
            mmSocket?.outputStream?.write("CloseBluetooth".toByteArray(Charsets.UTF_8))
            mmSocket?.close()
        } catch (e: IOException) {
            Log.e("ConnectThread", "Error closing socket: ${e.message}")
        }
    }
}

