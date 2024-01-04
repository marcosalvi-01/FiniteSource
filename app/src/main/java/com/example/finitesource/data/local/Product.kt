package com.example.finitesource.data.local

interface Product {
}

enum class Products(val productId: String) : Product {
	FINITE_SOURCE("finite-source"),
	SCENARIOS("scenarios"),
	FOOTPRINTS("footprints");

	companion object {
		fun parseString(string: String): Products = try {
			entries.first { it.productId == string }
		} catch (e: NoSuchElementException) {
			throw IllegalArgumentException("No product with id $string")
		}
	}
}