package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.EarthquakeDao
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.openapitools.client.apis.FiniteSourceAndroidAppApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.FiniteSourceAppAppJsonGet200ResponseInner
import retrofit2.Response

class EarthquakesRepositoryTest {

	private lateinit var mockkApiClient: ApiClient

	@Before
	fun setUp() {
		mockkApiClient = mockk<ApiClient>()
	}

	@After
	fun tearDown() {

	}

	@Test
	fun `test getAll`() {
		runTest {
			// mockk the database
			val mockkEarthquakesRepository = mockkEarthquakeRepository(testEarthquakes)
			// test the repository
			val earthquakes = mockkEarthquakesRepository.getAll().first()
			// assert that the earthquakes are correct
			assertEquals(testEarthquakes, earthquakes)
		}
	}

	@Test
	fun `test getById`() {
		runTest {
			// mockk the database
			val mockkEarthquakesRepository = mockkEarthquakeRepository(testEarthquakes)
			// test the repository
			val earthquake = mockkEarthquakesRepository.getById(testEarthquakes.first().id).first()
			// assert that the earthquake is correct
			assertEquals(testEarthquakes.first(), earthquake)
		}
	}

	@Test
	fun `test updateEarthquakes successful with new events`() {
		runTest {
			// this mockk is what the database would return
			val mockkEarthquakesRepository = mockkEarthquakeRepository(listOf(testEarthquakes[0]))
			val mockkResponse = mockk<Response<List<FiniteSourceAppAppJsonGet200ResponseInner>>>()
			every {
				mockkApiClient.createService(FiniteSourceAndroidAppApi::class.java)
					.finiteSourceAppAppJsonGet().execute()
			} returns mockkResponse
			every { mockkResponse.isSuccessful } returns true
			// this mockk is what the server would return
			every { mockkResponse.body()!! } returns testEarthquakeResponses

			// compare the server response to the database response
			val updates = mockkEarthquakesRepository.updateEarthquakes()

			// assert that the updates are correct
			assert(updates!!.newEarthquakes.first().id == testEarthquakeResponses.last().idEvent)
		}
	}

	@Test
	fun `test updateEarthquakes successful with updated sources`() {
		runTest {
			// this mockk is what the database would return
			val mockkEarthquakesRepository = mockkEarthquakeRepository(testEarthquakes)
			val mockkResponse = mockk<Response<List<FiniteSourceAppAppJsonGet200ResponseInner>>>()
			every {
				mockkApiClient.createService(FiniteSourceAndroidAppApi::class.java)
					.finiteSourceAppAppJsonGet().execute()
			} returns mockkResponse
			every { mockkResponse.isSuccessful } returns true
			val testEarthquakeResponses = testEarthquakeResponses.map {
				// add a day to the finite source last updated of the event that have one
				it.copy(finiteSourceLastUpdated = it.finiteSourceLastUpdated?.plusDays(1))
			}
			// this mockk is what the server would return
			every { mockkResponse.body()!! } returns testEarthquakeResponses

			// compare the server response to the database response
			val updates = mockkEarthquakesRepository.updateEarthquakes()

			// assert that the updates are correct
			assert(updates!!.finiteSourceUpdated.first().id == testEarthquakeResponses.first().idEvent)
		}
	}

	@Test
	fun `test updateEarthquakes successful with both updates`() {
		runTest {
			// this mockk is what the database would return
			val mockkEarthquakesRepository = mockkEarthquakeRepository(listOf(testEarthquakes[0]))
			val mockkResponse = mockk<Response<List<FiniteSourceAppAppJsonGet200ResponseInner>>>()
			every {
				mockkApiClient.createService(FiniteSourceAndroidAppApi::class.java)
					.finiteSourceAppAppJsonGet().execute()
			} returns mockkResponse
			every { mockkResponse.isSuccessful } returns true
			val testEarthquakeResponses = testEarthquakeResponses.map {
				// add a day to the finite source last updated of the event that have one
				it.copy(finiteSourceLastUpdated = it.finiteSourceLastUpdated?.plusDays(1))
			}
			// this mockk is what the server would return
			every { mockkResponse.body()!! } returns testEarthquakeResponses

			// compare the server response to the database response
			val updates = mockkEarthquakesRepository.updateEarthquakes()

			// assert that the updates are correct
			assert(updates!!.newEarthquakes.first().id == testEarthquakeResponses.last().idEvent)
			assert(updates.finiteSourceUpdated.first().id == testEarthquakeResponses.first().idEvent)
		}
	}

	@Test
	fun `test updateEarthquakes successful with no updates`() {
		runTest {
			// this mockk is what the database would return
			val mockkEarthquakesRepository = mockkEarthquakeRepository(testEarthquakes)
			val mockkResponse = mockk<Response<List<FiniteSourceAppAppJsonGet200ResponseInner>>>()
			every {
				mockkApiClient.createService(FiniteSourceAndroidAppApi::class.java)
					.finiteSourceAppAppJsonGet().execute()
			} returns mockkResponse
			every { mockkResponse.isSuccessful } returns true
			// this mockk is what the server would return
			every { mockkResponse.body()!! } returns testEarthquakeResponses

			// compare the server response to the database response
			val updates = mockkEarthquakesRepository.updateEarthquakes()

			// assert that the updates are correct
			assert(!updates!!.hasUpdates())
		}
	}

	@Test
	fun `test updateEarthquakes network failure returns null`() {
		runTest {
			// this mockk is what the database would return
			val mockkEarthquakesRepository = mockkEarthquakeRepository(testEarthquakes)
			val mockkResponse = mockk<Response<List<FiniteSourceAppAppJsonGet200ResponseInner>>>()
			every {
				mockkApiClient.createService(FiniteSourceAndroidAppApi::class.java)
					.finiteSourceAppAppJsonGet().execute()
			} returns mockkResponse
			// make the network call fail
			every { mockkResponse.isSuccessful } returns false

			// compare the server response to the database response
			val updates = mockkEarthquakesRepository.updateEarthquakes()

			// assert that the updates are correct
			assertEquals(null, updates)
		}
	}

	// function to create a mockk of the database that returns the test earthquakes
	private fun mockkEarthquakeRepository(testEarthquakes: List<Earthquake>): EarthquakesRepository {
		val mockEarthquakeDao = mockk<EarthquakeDao>()
		every { mockEarthquakeDao.getAll() } returns flow { emit(testEarthquakes) }
		every { mockEarthquakeDao.upsertAll(any()) } returns Unit
		every { mockEarthquakeDao.getById(any()) } returns flow {
			emit(testEarthquakes.first())
		}
		return EarthquakesRepository(mockEarthquakeDao, mockkApiClient)
	}
}
