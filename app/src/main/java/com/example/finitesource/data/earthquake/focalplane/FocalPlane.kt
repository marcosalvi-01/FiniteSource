package com.example.finitesource.data.earthquake.focalplane

import androidx.room.Embedded

//abstract class FocalPlane(
//	@Embedded(prefix = "scenarios_") val scenarios: Scenarios,
//	@Embedded(prefix = "finite_source_") val finiteSource: FiniteSource,
//)

class FocalPlane(
	val focalPlaneType: FocalPlaneType,
	@Embedded(prefix = "scenarios_") val scenarios: Scenarios?,
	@Embedded(prefix = "finite_source_") val finiteSource: FiniteSource?,
)

enum class FocalPlaneType { FP1, FP2, }