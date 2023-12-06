package com.example.finitesource.data.earthquake.focalplane

import com.example.finitesource.data.Product

data class FiniteSource(
	val inversionDescription: String,
	val mainInversionMapImageUrl: String,
	val slipDistributionImageUrl: String,
	val resultDescription: String,
// TODO	@Embedded val geoJsonReader: JsonReader,
) : Product