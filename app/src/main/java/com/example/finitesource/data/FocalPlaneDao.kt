package com.example.finitesource.data

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface FocalPlaneDao {
	@Insert
	fun insert(focalPlane: FocalPlane)
}