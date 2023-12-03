package com.example.finitesource

import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Date

fun offsetDateTimeToCalendar(offsetDateTime: OffsetDateTime?): Calendar? {
	if (offsetDateTime == null)
		return null
	val date = Date.from(offsetDateTime.toInstant())
	val calendar = Calendar.getInstance()
	calendar.time = date
	return calendar
}