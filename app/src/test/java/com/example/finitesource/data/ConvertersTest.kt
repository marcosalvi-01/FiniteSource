package com.example.finitesource.data

import com.example.finitesource.data.local.database.Converters
import com.example.finitesource.data.local.earthquake.focalplane.Scenario
import com.example.finitesource.data.local.earthquake.focalplane.ScenarioType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.SEPTEMBER
import java.util.Calendar.YEAR

internal class ConvertersTest {

	private val cal = Calendar.getInstance().apply {
		set(YEAR, 1998)
		set(MONTH, SEPTEMBER)
		set(DAY_OF_MONTH, 4)
	}

	@Test
	fun `test calendarToTimestamp`() {
		assertEquals(cal.timeInMillis, Converters().calendarToDatestamp(cal))
	}

	@Test
	fun `test timestampToCalendar`() {
		assertEquals(Converters().datestampToCalendar(cal.timeInMillis), cal)
	}

	@Test
	fun `test scenarioToString`() {
		val scenario = Scenario(
			ScenarioType(
				"1",
				"2",
				"3"
			),
			"4",
			"5",
			"6",
			"7"
		)
		assertEquals(Converters().scenarioToString(scenario), "1,2,3,4,5,6,7")
	}

	@Test
	fun `test stringToScenario`() {
		val scenario = Scenario(
			ScenarioType(
				"1",
				"2",
				"3"
			),
			"4",
			"5",
			"6",
			"7"
		)
		assertEquals(Converters().stringToScenario("1,2,3,4,5,6,7"), scenario)
	}
}
