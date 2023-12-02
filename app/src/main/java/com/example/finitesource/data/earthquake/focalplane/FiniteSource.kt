package com.example.finitesource.data.earthquake.focalplane

data class FiniteSource(
	val inversionDescription: String,
	val mainInversionMapImageUrl: String,
	val slipDistributionImageUrl: String,
	val resultDescription: String,
// TODO	@Embedded val geoJsonReader: JsonReader,
)