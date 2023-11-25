package com.example.finitesource.data

import androidx.room.Entity

@Entity(tableName = "focal_plane", primaryKeys = ["earthquakeId"])
abstract class FocalPlane(
	val earthquakeId: String,
)

class FP1(earthquakeId: String) : FocalPlane(earthquakeId)
class FP2(earthquakeId: String) : FocalPlane(earthquakeId)