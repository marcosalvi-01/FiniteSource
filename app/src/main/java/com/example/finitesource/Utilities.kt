package com.example.finitesource

import android.icu.util.Calendar
import java.time.OffsetDateTime
import java.util.Date

fun offsetDateTimeToCalendar(offsetDateTime: OffsetDateTime?): Calendar? {
	if (offsetDateTime == null)
		return null
	return Calendar.getInstance().apply { time = Date.from(offsetDateTime.toInstant()) }
}