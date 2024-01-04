package com.example.finitesource.data.local.earthquake.focalplane

import com.example.finitesource.data.local.Product
import com.example.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson

class FiniteSource(
	val inversionDescription: String,
	val resultDescription: String,
	val mainInversionMapImageUrl: String,
	val slipDistributionImageUrl: String,
	val sourceJson: CustomGeoJson,
) : Product