package com.example.agrorob.adapters


import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import com.example.agrorob.BluetoothDevicesActivity
import com.example.agrorob.R
import com.example.agrorob.models.Settings
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt

class BluetoothDeviceSocket(private val context: Context, private val mmSocket: BluetoothSocket) {
    companion object {
        const val TAG = "AgroRobLogs"
    }

    private val mmInStream = mmSocket.inputStream
    private val mmOutStream = mmSocket.outputStream
    private var connected: Boolean? = false

    private val inflater = LayoutInflater.from(context)
    private val view = inflater.inflate(R.layout.popup_layout, null)

    private val title = view.findViewById<TextView>(R.id.popup_title)
    private val message = view.findViewById<TextView>(R.id.popup_message)
    private val button = view.findViewById<Button>(R.id.popup_button)

    private val myView: View = (context as Activity).findViewById(R.id.nav_host_fragment_activity_robo_main)
    private lateinit var msgText: TextView
    private lateinit var dataText: TextView
    private lateinit var data2Text: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private lateinit var receiveJob: Job

    fun setStatus(status: Boolean) {
        connected = status
    }
    // Call this method from the main activity to shut down the connection.
    fun cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
            Toast.makeText(context, "Could not close the client socket", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startReceivingData(socket: BluetoothSocket, callback: (String) -> Unit) {
        receiveJob = GlobalScope.launch(Dispatchers.IO) {
            val inputStream: InputStream = socket.inputStream
            val buffer = ByteArray(1024)
            var bytes: Int
            var partialData: String = ""

            while (isActive) {
                try {
                    try {
                        bytes = inputStream.read(buffer)
                    } catch (e: IOException) {
                        Log.e(TAG, "Error reading from input stream", e)
                        connected = false
                        Toast.makeText(context, "Unable to connect to device", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, BluetoothDevicesActivity::class.java)
                        context.startActivity(intent)
                        (context as Activity).finish()
                        // Handle the error case gracefully, for example by closing the socket and/or restarting the connection
                        break
                    }
                    if (bytes == -1) {
                        Log.e(TAG, "Input stream was closed unexpectedly")
                        connected = false
                        Toast.makeText(context, "Unable to connect to device", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, BluetoothDevicesActivity::class.java)
                        context.startActivity(intent)
                        (context as Activity).finish()
                        // Handle the error case gracefully, for example by closing the socket and/or restarting the connection
                        break
                    }
                    val data = buffer.sliceArray(0 until bytes).decodeToString()
                    withContext(Dispatchers.Main) {
                        callback(data)
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        msgText.text = "Press to start"
                        startButton.isVisible = true
                        stopButton.isVisible = false
                    }
                    Log.e(TAG, "Error occurred when receiving data", e)
                    break
                }
                delay(500) // wait for 0.1 seconds before receiving next data
            }
        }
    }

    fun stopReceivingData() {
        if (::receiveJob.isInitialized) {
            if(connected == true) {
                mmSocket.outputStream.write("RunningStop".toByteArray(Charsets.UTF_8))
                receiveJob.cancel()
            }
        }
    }

    private fun showPopup(msg: String) {
        title.text = "Error sensor"
        message.text = msg
        val popup = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        button.setOnClickListener {
            popup.dismiss()
        }
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private fun loadData(): Settings? {
        val gson = Gson()
        val jsonString =
            context.openFileInput("tf_mini_data.json")?.bufferedReader()?.use { it.readText() }
        return gson.fromJson(jsonString, Settings::class.java)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("InflateParams")
    fun startListeningSocket() {
        GlobalScope.launch(Dispatchers.IO) {
            startButton = myView.findViewById(R.id.start_button)
            stopButton = myView.findViewById(R.id.stop_button)
            msgText = myView.findViewById(R.id.startMainMsg)
            dataText = myView.findViewById(R.id.meanText)
            data2Text = myView.findViewById(R.id.diameterText)

            val outputStream: OutputStream = mmSocket.outputStream
            try {
                var message = "RunningPrepar"

                val cons = loadData()
                if(cons != null) {
                    for ((index, value) in cons.toMap().entries.withIndex()) {
                        Log.d(TAG, "Index: $index Value: $value")
                        message = "$message|$value"
                    }
                }
                Log.d(TAG, message)
                outputStream.write(message.toByteArray(Charsets.UTF_8))
                delay(500)

                Log.d(TAG, "start")
                withContext(Dispatchers.Main) {
                    msgText.text = "Started processing..."
                    startButton.isVisible = false
                    stopButton.isVisible = true
                }

                startReceivingData(mmSocket, ::handleData)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Unable to send data, check if device is connected!", Toast.LENGTH_SHORT).show()
                    msgText.text = "Press to start"
                    startButton.isVisible = true
                    stopButton.isVisible = false
                }
                Log.e(TAG, "Error occurred when sending data", e)
            }
        }
    }

    private fun handleData(data: String) {
        val values = data.split(";")
        for (value in values) {
            val res = value.split(":")
            if(res.isNotEmpty() && res.size > 1) {
                if (res[0] == "mean") {
                    changeMean(res[1])
                } else if (res[0] == "cilindr"){
                    changeMean(res[1])
                }  else if (res[0] == "hidtrue"){
                    Toast.makeText(context, "Size increased: ${res[1]}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, res.toString())
                } else if (res[0] == "hidfalse"){
                    Toast.makeText(context, "Size reduced: ${res[1]}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, res.toString())
                }
            }
        }
    }

    private fun changeMean(data: String) {
        // handle received data here
        if (data == "TFminiError") {
            Log.e(TAG, "Can not found MAIN sensor",)
            msgText.text = "Can not found MAIN sensor"
        } else if (data == "TFmini2Error") {
            Log.d(TAG, "Can not found SECOND sensor")
            msgText.text = "Can not found MAIN sensor"
        } else {
            Log.d(TAG, data)
            try {
                val mValue = data.substring(3)
                val type = data.substring(0, 3)

                val floatValue = mValue.toFloat()
                val roundoff = (floatValue * 100.0).roundToInt() / 100.0
                val roundedValue = String.format("%.2f", roundoff)
                if (type == "tf1") {
                    dataText.text = "${roundedValue}" //output on application
                } else {
                    data2Text.text = "${roundedValue}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in reading value")
            }
        }
    }

    suspend fun sendCommand(socket: BluetoothSocket, command: String) {
        return withContext(Dispatchers.IO) {
            val outputStream = socket.outputStream

            outputStream.write(command.toByteArray(Charsets.UTF_8))

            if(!::receiveJob.isInitialized || !receiveJob.isActive) {
                Log.d(TAG, "Command response")

                val inputStream: InputStream = socket.inputStream
                val buffer = ByteArray(1024)

                val bytes: Int = inputStream.read(buffer)
                val data = buffer.sliceArray(0 until bytes).decodeToString()
                withContext(Dispatchers.Main) {
                    handleData(data)
                }
            }
        }
    }
}