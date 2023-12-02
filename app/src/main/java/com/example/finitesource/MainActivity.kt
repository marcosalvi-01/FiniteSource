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
		}

		earthquakesViewModel.earthquakes.observe(this) {
			Log.d("MainActivity", "Earthquakes: ${it.size}")
		}
	}
}


