package com.example.finitesource

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.finitesource.viewmodels.EarthquakesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val earthquakesViewModel: EarthquakesViewModel by lazy {
        ViewModelProvider(this)[EarthquakesViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // do something with the updates
        earthquakesViewModel.getUpdates().observe(this) {
            if (it == null)
                Toast.makeText(
                    this@MainActivity,
                    "Failed to update earthquakes",    // TODO: use snackbar
                    Toast.LENGTH_SHORT
                ).show()
            Log.d("MainActivity", "Updates: $it")
        }

        earthquakesViewModel.earthquakes.observe(this) { earthquakes ->
            Log.d("MainActivity", "Earthquakes: ${earthquakes.size}")
            if (earthquakes.isNotEmpty())
                earthquakesViewModel.selectEarthquake(
                    earthquakes.first {
                        it.id == "202302201704_01"
                    }
                )
        }

        // observe the ui state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                earthquakesViewModel.uiState.collect {
                    Log.d("MainActivity", "Selected earthquake: $it")
                }
            }
        }
    }
}


