package com.example.finitesource.data.earthquake.focalplane

import androidx.room.Embedded

abstract class FocalPlane(
	@Embedded(prefix = "scenarios_") val scenarios: Scenarios,
	@Embedded(prefix = "finite_source_") val finiteSource: FiniteSource,
)

sealed class FocalPlaneType(
	scenarios: Scenarios,
	finiteSource: FiniteSource,
) : FocalPlane(scenarios, finiteSource){

	class FP1(
		scenarios: Scenarios,
		finiteSource: FiniteSource,
	) : FocalPlaneType(scenarios, finiteSource)

	class FP2(
		scenarios: Scenarios,
		finiteSource: FiniteSource,
	) : FocalPlaneType(scenarios, finiteSource)
}