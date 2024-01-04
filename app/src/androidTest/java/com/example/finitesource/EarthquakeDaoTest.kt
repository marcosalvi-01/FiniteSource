package com.example.finitesource

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.finitesource.data.local.database.AppDatabase
import com.example.finitesource.data.local.database.dao.EarthquakeDao
import com.example.finitesource.data.local.earthquake.Earthquake
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class EarthquakeDaoTest {
	private val testEarthquakes = listOf(
		Earthquake(
			"202302201704_01",
			"Turkey [Sea]",
			Calendar.getInstance().apply {
				set(Calendar.YEAR, 2023)
				set(Calendar.MONTH, Calendar.FEBRUARY)
				set(Calendar.DAY_OF_MONTH, 20)
				set(Calendar.HOUR_OF_DAY, 17)
				set(Calendar.MINUTE, 4)
				set(Calendar.SECOND, 0)
				set(Calendar.MILLISECOND, 0)
			},
			6.3000002,
			16.0,
			36.1094,
			36.0165,
			Calendar.getInstance().apply {
				set(Calendar.YEAR, 2023)
				set(Calendar.MONTH, Calendar.NOVEMBER)
				set(Calendar.DAY_OF_MONTH, 26)
				set(Calendar.HOUR_OF_DAY, 12)
				set(Calendar.MINUTE, 27)
				set(Calendar.SECOND, 59)
				set(Calendar.MILLISECOND, 0)
			},
		),
		Earthquake(
			"202310110041_01",
			"Northwestern Afghanistan [Land: Afghanistan]",
			Calendar.getInstance().apply {
				set(Calendar.YEAR, 2023)
				set(Calendar.MONTH, Calendar.OCTOBER)
				set(Calendar.DAY_OF_MONTH, 11)
				set(Calendar.HOUR_OF_DAY, 0)
				set(Calendar.MINUTE, 41)
				set(Calendar.SECOND, 58)
				set(Calendar.MILLISECOND, 0)
			},
			6.4000001,
			14.844,
			34.4461,
			62.0754,
			null,
		),
	)

	private lateinit var database: AppDatabase
	private lateinit var earthquakeDao: EarthquakeDao

	@Before
	fun setup() {
		val context = ApplicationProvider.getApplicationContext<Context>()
		database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
			.allowMainThreadQueries()
			.build()
		earthquakeDao = database.earthquakeDao()
	}

	@After
	fun tearDown() {
		database.close()
	}

	@Test
	fun testGetAll() {
		runTest {
			earthquakeDao.insertAll(testEarthquakes)
			earthquakeDao.getAll().firstOrNull().let {
				assertEquals(testEarthquakes, it)
			}
		}
	}

	@Test
	fun testInsert() {
		runTest {
			earthquakeDao.insert(testEarthquakes[0])
			earthquakeDao.getById(testEarthquakes[0].id).firstOrNull().let {
				assertEquals(testEarthquakes[0], it)
			}
		}
	}

	@Test
	fun testGetById() {
		runTest {
			earthquakeDao.insertAll(testEarthquakes)
			earthquakeDao.getById(testEarthquakes[0].id).firstOrNull().let {
				assertEquals(testEarthquakes[0], it)
			}
		}
	}
}