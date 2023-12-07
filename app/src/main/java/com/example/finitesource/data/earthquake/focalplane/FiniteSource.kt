package com.example.finitesource.data.earthquake.focalplane

import com.example.finitesource.data.Product
import com.example.finitesource.data.earthquake.focalplane.geojson.CustomGeoJson

data class FiniteSource(
	val inversionDescription: String,
	val resultDescription: String,
	val mainInversionMapImageUrl: String,
	val slipDistributionImageUrl: String,
	val sourceJson: CustomGeoJson,
) : Product