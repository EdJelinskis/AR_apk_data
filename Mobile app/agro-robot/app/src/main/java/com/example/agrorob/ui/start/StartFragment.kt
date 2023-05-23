package com.example.agrorob.ui.start

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.agrorob.R
import com.example.agrorob.adapters.BluetoothConnection
import com.example.agrorob.adapters.BluetoothDeviceSocket
import com.example.agrorob.databinding.FragmentStartBinding
import kotlinx.coroutines.launch

class StartFragment : Fragment() {

    private var _binding: FragmentStartBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: StartViewModel
    private lateinit var bluetoothDeviceSocket: BluetoothDeviceSocket
    private var currentDevice: BluetoothDevice? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val startViewModel =
            ViewModelProvider(this)[StartViewModel::class.java]

        _binding = FragmentStartBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val printDeviceButton: Button = view.findViewById(R.id.start_button)
        val stopDeviceButton: Button = view.findViewById(R.id.stop_button)
        val increaseButton: Button = view.findViewById(R.id.increase_hid)
        val decreaseButton: Button = view.findViewById(R.id.decrease_hid)
        val screenMsg: TextView = view.findViewById(R.id.startMainMsg)
        sharedViewModel = ViewModelProvider(requireActivity())[StartViewModel::class.java]

        sharedViewModel.bluetoothDevice.observe(viewLifecycleOwner, Observer { device ->
            currentDevice = device
        })

        val socket = BluetoothConnection.socket
        bluetoothDeviceSocket = context?.let { it1 -> socket?.let {
            BluetoothDeviceSocket(it1,
                it
            )
        } }!!

        // TODO set result message to some text view
        increaseButton.setOnClickListener {
            lifecycleScope.launch {
                socket?.let { it1 -> bluetoothDeviceSocket.sendCommand(it1, "HidManTrue") }
            }
        }
        decreaseButton.setOnClickListener {
            lifecycleScope.launch {
                socket?.let { it1 -> bluetoothDeviceSocket.sendCommand(it1, "HidManFalse") }
            }
        }

        printDeviceButton.isVisible = true
        printDeviceButton.setOnClickListener {
            currentDevice?.let { device ->
                if (socket != null && socket.isConnected) {
                    bluetoothDeviceSocket.setStatus(true)
                    // TODO on scoket error try reconnect
                    if(!socket.isConnected) {
                        socket.connect()
                    }
                    // TODO Results need to show in some text views
                    bluetoothDeviceSocket.startListeningSocket()
                } else {
                    // Not connected, handle the situation
                    Toast.makeText(requireContext(), "${device.address}: no opened connection", Toast.LENGTH_LONG).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "No device found", Toast.LENGTH_SHORT).show()
            }
        }

        stopDeviceButton.isVisible = false
        stopDeviceButton.setOnClickListener {
            currentDevice?.let { device ->
                if (socket != null && socket.isConnected) {
                    bluetoothDeviceSocket.stopReceivingData()
                    Toast.makeText(requireContext(), "Stopped automatic adjustment", Toast.LENGTH_LONG).show()
                    stopDeviceButton.isVisible = false
                    printDeviceButton.isVisible = true
                    screenMsg.text = "Press to start"
                } else {
                    // Not connected, handle the situation
                    Toast.makeText(requireContext(), "${device.address}: no opened connection", Toast.LENGTH_LONG).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "No device found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bluetoothDeviceSocket.stopReceivingData()
        _binding = null
    }
}