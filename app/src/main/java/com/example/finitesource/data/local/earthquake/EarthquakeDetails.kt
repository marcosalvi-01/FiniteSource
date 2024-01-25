package com.example.finitesource.data.local.earthquake

import androidx.room.Embedded
import com.example.finitesource.data.local.Products
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlane
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import java.util.Locale

// The separation is needed because the details should not be stored in the database forever
data class EarthquakeDetails(
	@Embedded(prefix = "fp1_") val fp1: FocalPlane?,
	@Embedded(prefix = "fp2_") val fp2: FocalPlane?,
	@Embedded(prefix = "footprints_") val footprints: Footprints?,
	val ingvId: Long? = null,
) {
	fun getDefaultFocalPlane(): FocalPlane {
		return fp1 ?: fp2!!
	}

	fun getFocalPlane(focalPlaneType: FocalPlaneType?): FocalPlane? {
		return when (focalPlaneType) {
			FocalPlaneType.FP1 -> fp1
			FocalPlaneType.FP2 -> fp2
			else -> null
		}
	}

	fun getAvailableProducts(vararg focalPlaneTypes: FocalPlaneType): List<Products> {
		val availableProducts = mutableListOf<Products>()
		for (focalPlaneType in focalPlaneTypes) {
			val focalPlane = getFocalPlane(focalPlaneType)
			if (focalPlane != null) {
				availableProducts.addAll(focalPlane.availableProducts)
			}
		}
		return availableProducts
	}

	fun getAvailableProducts(): List<Products> {
		return getAvailableProducts(FocalPlaneType.FP1, FocalPlaneType.FP2)
	}


	/**
	 * This function generates a URL for the INGV (Istituto Nazionale di Geofisica e Vulcanologia) earthquake event page.
	 * The URL is based on the ID of the earthquake event (`ingvId`).
	 *
	 * If the `ingvId` is null, the function returns null.
	 *
	 * If the default language of the device is not Italian, the function modifies the URL to point to the English version of the event page.
	 *
	 * @return A string representing the URL for the INGV earthquake event page, or null if `ingvId` is null.
	 */
	fun getIngvUrl(): String? {
		if (ingvId == null)
			return null
		return with("http://terremoti.ingv.it/event/$ingvId") {
			if (Locale.getDefault().language != Locale("it").language)
			// If the default language is not Italian, change the URL to the English version
				replace("/event", "/en/event")
			else
			// If the default language is Italian, return the original URL
				this
		}
	}

	init {
		// check if there is at least one focal plane
		require(fp1 != null || fp2 != null) {
			"Earthquake must have at least one focal plane"
		}
	}
}