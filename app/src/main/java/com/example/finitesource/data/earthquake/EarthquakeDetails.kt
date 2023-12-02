package com.example.finitesource.data.earthquake

import androidx.room.Embedded
import com.example.finitesource.data.earthquake.focalplane.FocalPlaneType

// The separation is needed because the details should not be stored in the database forever
data class EarthquakeDetails(
	@Embedded(prefix = "fp1_") val fp1: FocalPlaneType.FP1?,
	@Embedded(prefix = "fp2_") val fp2: FocalPlaneType.FP2?,
	@Embedded(prefix = "footprints_") val footprints: Footprints,
) {
	init {
		// check if there is at least one focal plane
		require(fp1 != null || fp2 != null) {
			"Earthquake must have at least one focal plane"
		}
	}
}