package com.example.finitesource.data

import com.example.finitesource.offsetDateTimeToCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Date

internal class UtilitiesTest {
	@Test
	fun `test offsetDateTimeToCalendar with null input`() {
		val result = offsetDateTimeToCalendar(null)
		assertNull(result)
	}


	@Test
	fun `test offsetDateTimeToCalendar with valid input`() {
		val offsetDateTime = OffsetDateTime.parse("2023-01-01T12:00:00Z")
		val resultCalendar = offsetDateTimeToCalendar(offsetDateTime)
		val expectedCalendar =
			Calendar.getInstance().apply { time = Date.from(offsetDateTime.toInstant()) }
		assertEquals(expectedCalendar, resultCalendar)
	}
}