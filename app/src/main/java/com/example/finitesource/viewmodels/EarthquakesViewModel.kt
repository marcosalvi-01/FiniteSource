package com.example.finitesource.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.finitesource.data.EarthquakesRepository
import com.example.finitesource.data.earthquake.EarthquakeUpdates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class EarthquakesViewModel @Inject constructor(private val repository: EarthquakesRepository) :
	ViewModel() {

	val earthquakes = repository.getAll().asLiveData()

	// loads the latest data from the finite source api and compares it to the saved data
	// returns the differences that are supposed to be shown to the user
	fun getUpdates(): LiveData<EarthquakeUpdates?> =
		// build the live data that will emit the updates
		liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
			// update the database and emit the result
			emit(repository.updateEarthquakes())
		}
}