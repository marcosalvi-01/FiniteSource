package com.example.finitesource.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.finitesource.data.EarthquakesRepository
import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.EarthquakeUpdates
import com.example.finitesource.data.earthquake.focalplane.FocalPlaneType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EarthquakesViewModel @Inject constructor(
	private val repository: EarthquakesRepository
) : ViewModel() {

	val earthquakes = repository.getAll().asLiveData()

	private val _uiState = MutableLiveData<UiState>()
	val uiState: LiveData<UiState> = _uiState

	// loads the latest data from the finite source api and compares it to the saved data
	// returns the differences that are supposed to be shown to the user
	fun getUpdates(): LiveData<EarthquakeUpdates?> =
		// build the live data that will emit the updates
		liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
			// update the database and emit the result
			emit(repository.getUpdates())
		}

	/**
	 * Selects the earthquake and loads its details if necessary.
	 * It throws an exception if there is an error loading the details.
	 */
	fun selectEarthquake(earthquake: Earthquake, _focalPlaneType: FocalPlaneType? = null) {
		// if the earthquake is already selected, do nothing
		if (earthquake == _uiState.value?.selectedEarthquake)
			return
		// select the earthquake
		// if the selected earthquake doesn't have the details
		if (earthquake.details == null) {
			viewModelScope.launch(Dispatchers.IO) {
				// set the loading state
				_uiState.postValue(UiState(earthquake, _focalPlaneType, true))
				// load the details
				val loadedEarthquake = repository.loadEarthquakeDetails(earthquake.id)
					?: throw Exception("Failed to load earthquake details")
				// if the focal plane type is not specified, use the default one
				val focalPlaneType =
					_focalPlaneType
						?: loadedEarthquake.details!!.getDefaultFocalPlane().focalPlaneType
				// set the state
				_uiState.postValue(UiState(loadedEarthquake, focalPlaneType, false))
			}
		} else {
			// if the focal plane type is not specified, use the default one
			val focalPlaneType =
				_focalPlaneType ?: earthquake.details!!.getDefaultFocalPlane().focalPlaneType
			// if the selected earthquake has the details, set the state
			_uiState.value = UiState(earthquake, focalPlaneType, false)
		}
	}
}