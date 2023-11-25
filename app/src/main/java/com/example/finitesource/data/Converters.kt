package com.example.finitesource.data

import androidx.room.TypeConverter
import java.util.Calendar

class Converters {
	@TypeConverter
	fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

	@TypeConverter
	fun datestampToCalendar(value: Long): Calendar =
		Calendar.getInstance().apply { timeInMillis = value }
}