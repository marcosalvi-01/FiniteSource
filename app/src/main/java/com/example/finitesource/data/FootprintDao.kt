package com.example.finitesource.data

import androidx.room.Insert

interface FootprintDao {
	@Insert
	fun insert(footprint: Footprint)
}