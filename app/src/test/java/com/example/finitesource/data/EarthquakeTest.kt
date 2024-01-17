package com.example.finitesource.data

import com.example.finitesource.data.local.earthquake.toEarthquake
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.openapitools.client.models.FiniteSourceAppAppJsonGet200ResponseInner
import java.time.OffsetDateTime

internal class EarthquakeTest {

	@Test
	fun `test Earthquake at least one focal plane`() {
//		val exception = assertThrows(IllegalArgumentException::class.java) {
//			Earthquake(
//				"201504250611_01",
//				"Nepal",
//				Calendar.getInstance(),
//				12.0,
//				12.0,
//				12.0,
//				12.0,
//				null,
//				EarthquakeDetails(
//					null,
//					null,
//					Footprints("", ""),
//				)
//			)
//		}

//		assertEquals("Earthquake must have at least one focal plane", exception.message)
	}

	@Test
	fun `test toEarthquake`() {
		// mock the response
		val response = mockk<FiniteSourceAppAppJsonGet200ResponseInner>()
		setup(response)
		// use the mocked response to call the function toEarthquake and test that it returns an Earthquake
		val earthquake = toEarthquake(response)
		assertNotNull(earthquake)
		assertEquals("testId", earthquake?.id)
	}

	// helper function to setup the mock response
	private fun setup(mockResponse: FiniteSourceAppAppJsonGet200ResponseInner) {
		val now = OffsetDateTime.now()
		every { mockResponse.idEvent } returns "testId"
		every { mockResponse.name } returns "testName"
		every { mockResponse.occurringTime } returns now
		every { mockResponse.magnitude } returns 5.0
		every { mockResponse.depth } returns 10.0
		every { mockResponse.latitude } returns 12.34
		every { mockResponse.longitude } returns 56.78
		every { mockResponse.finiteSourceLastUpdated } returns now
	}
}