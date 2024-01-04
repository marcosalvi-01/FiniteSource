package com.example.finitesource

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.finitesource.data.CatalogConfig
import com.example.finitesource.viewmodels.EarthquakesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

	private val earthquakesViewModel: EarthquakesViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// load the config
		// TODO block the app until the config and the earthquake data are loaded (show a loading screen)
		lifecycleScope.launch {
			CatalogConfig.init(this@MainActivity)
		}

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
				try {
					earthquakesViewModel.selectEarthquake(
						earthquakes.first {
							it.id == "202302201704_01"
						}
					)
				} catch (e: Exception) {
					Log.d("MainActivity", "Failed to load earthquake ${e.message}")
				}
		}

		earthquakesViewModel.uiState.observe(this) {
			Log.d("MainActivity", "Selected earthquake: $it")
		}
	}
}


