package com.example.agrorob.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrorob.R

@SuppressLint("MissingPermission")
class BluetoothDeviceAdapter(
    private var devices: MutableList<BluetoothDevice>,
    private val onItemClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        val deviceMac: TextView = itemView.findViewById(R.id.deviceMac)
    }

    val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                if (!devices.contains(device)) {
                    devices.add(device)
                    notifyItemInserted(devices.size - 1)
                }
            }
        }
    }

    init {
        // Add all paired devices to the list
        // Made changes
        devices.addAll(BluetoothAdapter.getDefaultAdapter().bondedDevices)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.name ?: "Unknown Device"
        holder.deviceMac.text = device.address ?: "00:00:00:00:00"
        holder.itemView.setOnClickListener { onItemClick(device) }
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}
