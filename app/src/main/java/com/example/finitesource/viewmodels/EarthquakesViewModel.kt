package com.example.finitesource.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.finitesource.data.EarthquakesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EarthquakesViewModel @Inject constructor(repository: EarthquakesRepository) :
	ViewModel() {

	val earthquakes = repository.getAll().asLiveData()


}