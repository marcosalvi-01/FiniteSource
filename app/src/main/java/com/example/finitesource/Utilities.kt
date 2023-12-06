package com.example.finitesource

import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun offsetDateTimeToCalendar(offsetDateTime: OffsetDateTime?): Calendar? {
	if (offsetDateTime == null)
		return null
	val date = Date.from(offsetDateTime.toInstant())
	val calendar = Calendar.getInstance()
	calendar.time = date
	return calendar
}

// Helper function to get the locale suffix for the text files
fun getLocaleSuffix(): String {
	return when (Locale.getDefault().language) {
		"it" -> "it"
		else -> "en"
	}
}