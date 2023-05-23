package com.example.agrorob.ui.start

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StartViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    private val _bluetoothDevice = MutableLiveData<BluetoothDevice>().apply {
        value = null
    }
    val bluetoothDevice: LiveData<BluetoothDevice> = _bluetoothDevice

    fun setBluetoothDevice(device: BluetoothDevice) {
        _bluetoothDevice.value = device
    }
}