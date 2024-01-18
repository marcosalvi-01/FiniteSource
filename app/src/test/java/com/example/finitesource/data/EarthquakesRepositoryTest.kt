package com.example.finitesource.data

import com.example.finitesource.data.local.database.dao.EarthquakeDao
import com.example.finitesource.data.local.database.dao.ScenarioTypeDao
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.openapitools.client.infrastructure.ApiClient

class EarthquakesRepositoryTest {


	@Test
	fun `getAll returns all earthquakes from the database`() = runTest {
		val mockkEarthquakeDao = mockk<EarthquakeDao>()
		val mockkScenarioTypeDao = mockk<ScenarioTypeDao>()
		val mockkApiClient = mockk<ApiClient>()
		val earthquakesRepository =
			EarthquakesRepository(mockkEarthquakeDao, mockkScenarioTypeDao, mockkApiClient)

		every { mockkEarthquakeDao.getAll() } returns flowOf(testEarthquakes)

		val result = earthquakesRepository.getAll().first()

		assertEquals(testEarthquakes, result)
	}

	@Test
	fun `getById returns the correct earthquake from the database`() = runTest {
		val mockkEarthquakeDao = mockk<EarthquakeDao>()
		val mockkScenarioTypeDao = mockk<ScenarioTypeDao>()
		val mockkApiClient = mockk<ApiClient>()
		val earthquakesRepository =
			EarthquakesRepository(mockkEarthquakeDao, mockkScenarioTypeDao, mockkApiClient)

		val testEarthquake = testEarthquakes.first()
		every { mockkEarthquakeDao.getById(testEarthquake.id) } returns flowOf(testEarthquake)

		val result = earthquakesRepository.getById(testEarthquake.id).first()

		assertEquals(testEarthquake, result)
	}

	@Test
	fun `getById returns null when the earthquake does not exist in the database`() = runTest {
		val mockkEarthquakeDao = mockk<EarthquakeDao>()
		val mockkScenarioTypeDao = mockk<ScenarioTypeDao>()
		val mockkApiClient = mockk<ApiClient>()
		val earthquakesRepository =
			EarthquakesRepository(mockkEarthquakeDao, mockkScenarioTypeDao, mockkApiClient)

		val nonExistentEarthquakeId = "non-existent-id"
		every { mockkEarthquakeDao.getById(nonExistentEarthquakeId) } returns flowOf()

		val result = earthquakesRepository.getById(nonExistentEarthquakeId).firstOrNull()

		assertNull(result)
	}

	// TODO test getUpdates
}
