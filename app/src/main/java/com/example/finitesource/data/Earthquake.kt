package com.example.finitesource.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar


@Entity
data class Earthquake(
	@PrimaryKey val id: String,
	val name: String,
	val date: Calendar,    // instead of Date
	@Embedded(prefix = "fp1_") val fp1: FP1?,
	@Embedded(prefix = "fp2_") val fp2: FP2?,
	@Embedded(prefix = "footprint_") val footprint: Footprint,
) {
	init {
		// check if there is at least one focal plane
		require(fp1 != null || fp2 != null) {
			"Earthquake must have at least one focal plane"
		}
	}
}