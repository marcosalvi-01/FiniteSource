package it.ingv.finitesource.data.local.earthquake.focalplane

import androidx.room.Embedded
import it.ingv.finitesource.data.local.Products

//abstract class FocalPlane(
//	@Embedded(prefix = "scenarios_") val scenarios: Scenarios,
//	@Embedded(prefix = "finite_source_") val finiteSource: FiniteSource,
//)

class FocalPlane(
	val focalPlaneType: FocalPlaneType,
	@Embedded(prefix = "scenarios_") val scenarios: Scenarios?,
	@Embedded(prefix = "finite_source_") val finiteSource: FiniteSource?,
	val availableProducts: List<Products>,
)

enum class FocalPlaneType { FP1, FP2, }
