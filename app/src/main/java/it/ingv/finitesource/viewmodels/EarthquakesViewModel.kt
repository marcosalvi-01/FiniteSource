package it.ingv.finitesource.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import it.ingv.finitesource.data.EarthquakesRepository
import it.ingv.finitesource.data.local.EarthquakeUpdates
import it.ingv.finitesource.data.local.earthquake.Earthquake
import it.ingv.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import it.ingv.finitesource.states.LoadingState
import it.ingv.finitesource.states.UiState
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

	val updates: EarthquakeUpdates?
		get() = repository.catalogUpdates

	// loads the latest data from the finite source api and compares it to the saved data
	// returns the differences that are supposed to be shown to the user
	fun getUpdatesLiveData(): LiveData<EarthquakeUpdates?> =
		// build the live data that will emit the updates
		liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
			// update the database and emit the result
			emit(repository.getUpdatesFromRemote())
		}

	fun isFirstRun(firstRun: Boolean) = repository.isFirstRun(firstRun)

	/**
	 * Selects the earthquake and loads its details if necessary.
	 * It throws an exception if there is an error loading the details.
	 */
	fun selectEarthquake(earthquake: Earthquake, _focalPlaneType: FocalPlaneType? = null) {
		// if the earthquake is already selected, do nothing
		if (earthquake == _uiState.value?.selectedEarthquake)
			return
		// if there is a selected earthquake, deselect it
		if (_uiState.value?.selectedEarthquake != null)
			deselectEarthquake()
		// set the loading state
		_uiState.value = UiState(earthquake, _focalPlaneType, LoadingState())
		// select the earthquake
		// if the selected earthquake doesn't have the details
		if (earthquake.details == null) {
			viewModelScope.launch(Dispatchers.IO) {
				// load the details
				val loadedEarthquake = repository.loadEarthquakeDetails(earthquake.id)
				// if there was an error while loading the event details
				if (loadedEarthquake == null) {
					// set the error state
					_uiState.postValue(
						UiState(
							earthquake, _focalPlaneType, LoadingState(
								loading = false,
								errorWhileLoading = true
							)
						)
					)
					return@launch
				}
				// if the focal plane type is not specified, use the default one
				val focalPlaneType =
					_focalPlaneType
						?: loadedEarthquake.details!!.getDefaultFocalPlane().focalPlaneType
				// set the state
				_uiState.postValue(
					UiState(
						loadedEarthquake, focalPlaneType, LoadingState(loading = false)
					)
				)
			}
		} else {
			// if the focal plane type is not specified, use the default one
			val focalPlaneType =
				_focalPlaneType ?: earthquake.details!!.getDefaultFocalPlane().focalPlaneType
			// if the selected earthquake has the details, set the state
			_uiState.value = UiState(earthquake, focalPlaneType, LoadingState(loading = false))
		}
	}

	fun deselectEarthquake() {
		// set the selected earthquake in the state to null
		_uiState.value = UiState()
	}

	/**
	 * This function is used to select a specific focal plane type for the currently selected earthquake.
	 * If there is no selected earthquake or the focal plane type is already selected, the function will return without making any changes.
	 * Otherwise, it will update the UI state with the new focal plane type and set the loading state to false.
	 *
	 * @param focalPlaneType The type of the focal plane to be selected.
	 */
	fun selectFocalPlane(focalPlaneType: FocalPlaneType) {
		// If there is no selected earthquake, the function will return without making any changes.
		if (_uiState.value?.selectedEarthquake == null)
			return
		// If the focal plane type is already selected, the function will return without making any changes.
		if (_uiState.value?.selectedFocalPlane == focalPlaneType)
			return
		// The UI state is updated with the new focal plane type and the loading state is set to false.
		_uiState.value = UiState(
			_uiState.value!!.selectedEarthquake,
			focalPlaneType,
			LoadingState(loading = false)
		)
	}

	/**
	 * Initiates the download of the data_and_model.zip file for a specific earthquake and focal plane type.
	 *
	 * This function uses the repository to start the download of a zip file for the given earthquake and focal plane type.
	 * The result of the download is emitted as a LiveData<Boolean> object. The Boolean value indicates whether the download was successful or not.
	 *
	 * @param earthquake The earthquake for which the zip file is to be downloaded.
	 * @param focalPlaneType The type of the focal plane for which the zip file is to be downloaded.
	 * @return A LiveData<Boolean> object that emits the result of the download. The Boolean value is true if the download was successful, false otherwise.
	 */
	fun downloadZipToFile(
		earthquake: Earthquake,
		focalPlaneType: FocalPlaneType
	): LiveData<Boolean> =
		liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
			emit(repository.downloadZipToFile(earthquake, focalPlaneType))
		}
}
