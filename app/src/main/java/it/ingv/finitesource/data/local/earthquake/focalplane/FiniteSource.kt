package it.ingv.finitesource.data.local.earthquake.focalplane

import it.ingv.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson

class FiniteSource(
	val inversionDescription: String,
	val resultDescription: String,
	val mainInversionMapImageUrl: String,
	val slipDistributionImageUrl: String,
	val sourceJson: CustomGeoJson,
)
