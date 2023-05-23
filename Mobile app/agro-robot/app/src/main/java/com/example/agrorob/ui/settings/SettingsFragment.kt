package com.example.agrorob.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.agrorob.databinding.FragmentSettingsBinding
import com.example.agrorob.models.Settings
import com.example.agrorob.R
import com.google.gson.Gson
import java.io.File
import java.io.FileNotFoundException

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[SettingsViewModel::class.java]

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val settings = loadData()
        if(settings != null) {
            binding.numberEn.setText(settings.errorNegative.toString())
            binding.numberEp.setText(settings.errorPositive.toString())
            binding.numberAvg.setText(settings.average.toString())
            binding.numberCin.setText(settings.cylinderIn.toString())
            binding.numberCout.setText(settings.cylinderOut.toString())
        }

        val saveButton: Button = binding.saveData
        saveButton.setOnClickListener {
            val en = binding.numberEn.text.toString()
            val ep = binding.numberEp.text.toString()
            val avg = binding.numberAvg.text.toString()
            val cin = binding.numberCin.text.toString()
            val cout = binding.numberCout.text.toString()

            saveData(ep, en, avg, cin, cout)
            Toast.makeText(context, "Data saved!", Toast.LENGTH_SHORT).show()

            // as per defined in your FragmentContainerView
            val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment_activity_robo_main) as NavHostFragment
            val navController = navHostFragment.navController

            // Navigate using the IDs you defined in your Nav Graph
            navController.navigate(R.id.navigation_start)
        }

        binding.increaseEp.setOnClickListener {
            val number = binding.numberEp.text.toString().toIntOrNull()
            if (number != null) {
                val result = (number + 1).toString()
                binding.numberEp.setText(result)
            }
        }
        binding.decreaseEp.setOnClickListener {
            val number = binding.numberEp.text.toString().toIntOrNull()
            if (number != null) {
                val result = (number - 1).toString()
                binding.numberEp.setText(result)
            }
        }

        binding.increaseEn.setOnClickListener {
            val number = binding.numberEn.text.toString().toIntOrNull()
            if (number != null) {
                val result = (number + 1).toString()
                binding.numberEn.setText(result)
            }
        }
        binding.decreaseEn.setOnClickListener {
            val number = binding.numberEn.text.toString().toIntOrNull()
            if (number != null) {
                val result = (number - 1).toString()
                binding.numberEn.setText(result)
            }
        }

        binding.increaseAvg.setOnClickListener {
            val number = binding.numberAvg.text.toString().toIntOrNull()
            if (number != null) {
                val result = (number + 1).toString()
                binding.numberAvg.setText(result)
            }
        }
        binding.decreaseAvg.setOnClickListener {
            val number = binding.numberAvg.text.toString().toIntOrNull()
            if (number != null) {
                val result = (number - 1).toString()
                binding.numberAvg.setText(result)
            }
        }

        binding.increaseCin.setOnClickListener {
            val number = binding.numberCin.text.toString().toFloatOrNull()
            if (number != null) {
                val result = (number + 1.0).toString()
                binding.numberCin.setText(result)
            }
        }
        binding.decreaseCin.setOnClickListener {
            val number = binding.numberCin.text.toString().toFloatOrNull()
            if (number != null) {
                val result = (number - 1.0).toString()
                binding.numberCin.setText(result)
            }
        }

        binding.increaseCout.setOnClickListener {
            val number = binding.numberCout.text.toString().toFloatOrNull()
            if (number != null) {
                val result = (number + 1.0).toString()
                binding.numberCout.setText(result)
            }
        }
        binding.decreaseCout.setOnClickListener {
            val number = binding.numberCout.text.toString().toFloatOrNull()
            if (number != null) {
                val result = (number - 1.0).toString()
                binding.numberCout.setText(result)
            }
        }

        return root
    }

    private fun loadData(): Settings {
        val gson = Gson()
        val filename = "tf_mini_data.json"
        return try {
            val jsonString = context?.openFileInput(filename)?.bufferedReader()?.use { it.readText() }
            gson.fromJson(jsonString, Settings::class.java) ?: getDefaultSettings(filename)
        } catch (e: FileNotFoundException) {
            getDefaultSettings(filename)
        }
    }

    private fun getDefaultSettings(filename: String): Settings {
        val defaultSettings = Settings(
            errorPositive = 10,
            errorNegative = 5,
            average = 3,
            cylinderIn = 150.0,
            cylinderOut = 190.0
        )
        val gson = Gson()
        val jsonString = gson.toJson(defaultSettings)

        // Create the file and write the default settings to it
        context?.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it?.write(jsonString.toByteArray())
        }

        return defaultSettings
    }

    private fun saveData(ep: String, en: String, avg: String, cin: String, cout: String) {
        val person = Settings(
            ep.toInt(),
            en.toInt(),
            avg.toInt(),
            cin.toDouble(),
            cout.toDouble()
        )
        val gson = Gson()
        val json = gson.toJson(person)
        val file = context?.filesDir?.let { File(it, "tf_mini_data.json") }
        file?.writeText(json)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}