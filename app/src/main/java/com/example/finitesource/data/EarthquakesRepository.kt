package com.example.finitesource.data

import javax.inject.Inject

class EarthquakesRepository @Inject constructor(private val earthquakeDao: EarthquakeDao) {

	fun getAll() = earthquakeDao.getAll()

	fun getById(id: String) = earthquakeDao.getById(id)

	companion object {
		@Volatile
		private var instance: EarthquakesRepository? = null

		fun getInstance(earthquakeDao: EarthquakeDao) =
			instance ?: synchronized(this) {
				instance ?: EarthquakesRepository(earthquakeDao).also { instance = it }
			}
	}
}