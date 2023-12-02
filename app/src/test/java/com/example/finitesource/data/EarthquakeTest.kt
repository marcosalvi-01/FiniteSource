package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.EarthquakeDetails
import com.example.finitesource.data.earthquake.Footprints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Calendar

internal class EarthquakeTest {

//	private lateinit var earthquake: Earthquake
//
//	@Before
//	fun setUp() {
//		earthquake = Earthquake(
//			"201504250611_01",
//			"Nepal",
//			Calendar.getInstance(),
//			null,
//			FP2("201504250611_01"),
//			Footprint("201504250611_01"),
//		)
//	}

	@Test
	fun at_least_one_focal_plane() {
		val exception = assertThrows(IllegalArgumentException::class.java) {
			Earthquake(
				"201504250611_01",
				"Nepal",
				Calendar.getInstance(),
				EarthquakeDetails(
					null,
					null,
					Footprints("", ""),
				)
			)
		}

		assertEquals("Earthquake must have at least one focal plane", exception.message)
	}
}