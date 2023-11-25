package com.example.finitesource.data

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["earthquake_id"])
data class Footprint(
	@ColumnInfo(name = "earthquake_id") val earthquakeId: String,
)