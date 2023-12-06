package com.example.finitesource.data.earthquake.focalplane

import com.example.finitesource.data.Product

data class FiniteSource(
	val inversionDescription: String,
	val resultDescription: String,
	val mainInversionMapImageUrl: String,
	val slipDistributionImageUrl: String,
// TODO	@Embedded val geoJsonReader: JsonReader,
) : Product