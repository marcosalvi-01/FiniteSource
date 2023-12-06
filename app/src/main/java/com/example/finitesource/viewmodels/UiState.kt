package com.example.finitesource.viewmodels

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.focalplane.FocalPlaneType

// Defines the state of the app.
// The selected event, the selected focal plane
data class UiState(
	val selectedEarthquake: Earthquake? = null,
	val selectedFocalPlane: FocalPlaneType? = null,
	val earthquakeLoading: Boolean = true,
) {
	init {
		// if an earthquake is selected and it's not loading, a focal plane must be selected
		require(selectedEarthquake == null || earthquakeLoading || selectedFocalPlane != null) {
			"If an earthquake is selected and it is still loading, a focal plane must be selected"
		}
		// if the selected earthquake doesn't have the details, loading must be true
		require(selectedEarthquake == null || selectedEarthquake.details != null || earthquakeLoading) {
			"If the selected earthquake doesn't have the details, loading must be true"
		}
	}
}