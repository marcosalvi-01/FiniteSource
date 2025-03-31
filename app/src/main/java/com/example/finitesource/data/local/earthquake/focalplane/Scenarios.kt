package com.example.finitesource.data.local.earthquake.focalplane


data class Scenarios(
	val scenarios: List<Scenario>,
	val description: String? = null,
) {
	override fun toString(): String {
		val sb = StringBuilder()
		scenarios.forEach {
			sb.append(it.toString())
			sb.append("|")
		}
		return sb.toString()
	}
}

class Scenario(
	scenarioType: ScenarioType,
	val displacementMapDescription: String,
	val displacementMapUrl: String,
	val predictedFringesDescription: String?,
	val predictedFringesUrl: String?
) : ScenarioType(scenarioType.dir, scenarioType.name, scenarioType.url) {
	override fun toString(): String {
		return super.toString() +
				"|$displacementMapDescription|$displacementMapUrl" +
				"|$predictedFringesDescription|$predictedFringesUrl"
	}
}