package com.example.finitesource.data

sealed class Resource<T>(
	val data: T? = null,
	val message: String? = null
) {
	class Success<T>(data: T) : Resource<T>(data = data)
	class Error<T>(errorMessage: String) : Resource<T>(message = errorMessage)
	class Loading<T> : Resource<T>()

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Resource<*>) return false

		return data == other.data && message == other.message
	}

	override fun hashCode(): Int {
		var result = data?.hashCode() ?: 0
		result = 31 * result + (message?.hashCode() ?: 0)
		return result
	}
}
