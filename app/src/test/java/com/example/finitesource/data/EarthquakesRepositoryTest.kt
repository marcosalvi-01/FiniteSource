package com.example.finitesource.data

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

	private lateinit var mockkEarthquakesRepository: EarthquakesRepository
	private lateinit var mockkApiClient: ApiClient

	@Before
	fun setUp() {
		val mockEarthquakeDao = mockk<EarthquakeDao>()
		every { mockEarthquakeDao.getAll() } returns flow { emit(testEarthquakes) }
		every { mockEarthquakeDao.upsertAll(any()) } returns Unit
		every { mockEarthquakeDao.getById(any()) } returns flow {
			emit(testEarthquakes.first())
		}
		mockkApiClient = mockk<ApiClient>()

		mockkEarthquakesRepository = EarthquakesRepository(mockEarthquakeDao, mockkApiClient)
	}

	@After
	fun tearDown() {

	}

	@Test
	fun `test getAll`() {
		runTest {
			val earthquakes = mockkEarthquakesRepository.getAll().first()
			assertEquals(testEarthquakes, earthquakes)
		}
	}

	@Test
	fun `test getById`() {
		runTest {
			val earthquake = mockkEarthquakesRepository.getById(testEarthquakes.first().id).first()
			assertEquals(testEarthquakes.first(), earthquake)
		}
	}

	@Test
	fun `test updateEarthquakes successful with new events`() {
		TODO()
	}

	@Test
	fun `test updateEarthquakes successful with updated sources`() {
		TODO()
	}

	@Test
	fun `test updateEarthquakes successful with both updates`() {
		TODO()
	}

	@Test
	fun `test updateEarthquakes successful with no updates`() {
		runTest {
			val mockkResponse = mockk<Response<List<FiniteSourceAppAppJsonGet200ResponseInner>>>()
			every {
				mockkApiClient.createService(FiniteSourceAndroidAppApi::class.java)
					.finiteSourceAppAppJsonGet().execute()
			} returns mockkResponse
			every { mockkResponse.isSuccessful } returns true
			every { mockkResponse.body()!! } returns testEarthquakeResponses

			val updates = mockkEarthquakesRepository.updateEarthquakes()

			assert(!updates!!.hasUpdates())
		}
	}

	@Test
	fun `test updateEarthquakes network failure returns null`() {
		runTest {
			val mockkResponse = mockk<Response<List<FiniteSourceAppAppJsonGet200ResponseInner>>>()
			every {
				mockkApiClient.createService(FiniteSourceAndroidAppApi::class.java)
					.finiteSourceAppAppJsonGet().execute()
			} returns mockkResponse
			every { mockkResponse.isSuccessful } returns false

			val updates = mockkEarthquakesRepository.updateEarthquakes()

			assertEquals(null, updates)
		}
	}
}