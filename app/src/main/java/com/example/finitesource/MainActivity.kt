package com.example.finitesource

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.finitesource.viewmodels.EarthquakesViewModel
import dagger.hilt.android.AndroidEntryPoint

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

		earthquakesViewModel.uiState.observe(this) {
			Log.d("MainActivity", "Selected earthquake: $it")
		}
	}
}


