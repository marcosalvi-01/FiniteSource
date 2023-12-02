package com.example.finitesource.data.earthquake.focalplane

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
open class ScenarioType(
	@PrimaryKey var id: String,
	var name: String,
	var url: String
) {
	override fun toString(): String {
		return "$id,$name,$url"
	}
}