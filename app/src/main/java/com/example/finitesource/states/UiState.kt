package com.example.finitesource.states

import com.example.finitesource.data.local.earthquake.Earthquake
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType

// Defines the state of the app.
// The selected event, the selected focal plane
data class UiState(
	val selectedEarthquake: Earthquake? = null,
	val selectedFocalPlane: FocalPlaneType? = null,
	val loadingState: LoadingState = LoadingState(),
) {
	init {
		// if an earthquake is selected and it's not loading and there is no error, a focal plane must be selected
		require(selectedEarthquake == null || loadingState.loading || loadingState.errorWhileLoading || selectedFocalPlane != null) {
			"If an earthquake is selected and it's not loading and there is no error, a focal plane must be selected"
		}
		// if the selected earthquake doesn't have the details, loading must be true, unless there was an error
		require(selectedEarthquake == null || selectedEarthquake.details != null || loadingState.loading || loadingState.errorWhileLoading) {
			"If the selected earthquake doesn't have the details, loading must be true, unless there was an error"
		}
		// if there is no selected earthquake, there must be no selected focal plane
		require(selectedEarthquake != null || selectedFocalPlane == null) {
			"If there is no selected earthquake, there must be no selected focal plane"
		}
	}

	fun isLoaded(): Boolean {
		return !loadingState.loading && !loadingState.errorWhileLoading
	}
}

data class LoadingState(
	val loading: Boolean = true,
	val errorWhileLoading: Boolean = false,
)