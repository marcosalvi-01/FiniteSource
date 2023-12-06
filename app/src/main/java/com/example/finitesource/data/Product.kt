package com.example.finitesource.data

interface Product {
}

enum class Products(val productId: String) : Product {
	FINITE_SOURCE("finite-source"),
	SCENARIOS("scenarios"),
	FOOTPRINTS("footprints"),
}

// function to convert a list of strings to a list of products
fun List<String>.toProducts(): List<Products> {
	return this.map { it.toProduct() }
}

// function to convert a string to a product
fun String.toProduct(): Products {
	return Products.entries.first { it.productId == this }
}