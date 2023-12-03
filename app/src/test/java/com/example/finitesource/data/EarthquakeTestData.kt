package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import org.openapitools.client.models.FiniteSourceAppAppJsonGet200ResponseInner
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Date

val testEarthquakes = listOf(
	Earthquake(
		"202302201704_01",
		"Turkey [Sea]",
		Calendar.getInstance().apply {
			time = Date.from(OffsetDateTime.parse("2023-02-20T17:04:29Z").toInstant())
		},
		6.3000002,
		16.0,
		36.1094,
		36.0165,
		Calendar.getInstance().apply {
			time = Date.from(OffsetDateTime.parse("2023-11-26T12:27:59Z").toInstant())
		},
	),
	Earthquake(
		"202310110041_01",
		"Northwestern Afghanistan [Land: Afghanistan]",
		Calendar.getInstance().apply {
			time = Date.from(OffsetDateTime.parse("2023-10-11T00:41:58.846000Z").toInstant())
		},
		6.4000001,
		14.844,
		34.4461,
		62.0754,
		null,
	),
)

val testEarthquakeResponses = listOf(
	FiniteSourceAppAppJsonGet200ResponseInner(
		"202302201704_01",
		"Turkey [Sea]",
		OffsetDateTime.parse("2023-02-20T17:04:00Z"),
		6.3000002,
		16.0,
		36.1094,
		36.0165,
		listOf(
			36.0773,
			35.8748,
			36.3239,
			36.2123
		),
		OffsetDateTime.parse("2023-11-26T12:27:59Z"),
	),
	FiniteSourceAppAppJsonGet200ResponseInner(
		"202310110041_01",
		"Northwestern Afghanistan [Land: Afghanistan]",
		OffsetDateTime.parse("2023-10-11T00:41:58.846000Z"),
		6.4000001,
		14.844,
		34.4461,
		62.0754,
		listOf(
			34.5102,
			61.9489,
			34.6027,
			62.1407
		),
		null,
	),
)