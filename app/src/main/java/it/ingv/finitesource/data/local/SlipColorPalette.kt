package it.ingv.finitesource.data.local

class SlipColorPalette : ArrayList<Int>() {
	companion object {
		/**
		 * Parses an array into a [SlipColorPalette] object.
		 * Throws an exception if the array is not valid.
		 */
		fun fromList(list: List<String>): SlipColorPalette = try {
			SlipColorPalette().apply {
				list.forEach {
					// the string is in the format "#RRGGBB"
					add(it.substring(1).toInt(16))
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			throw Exception("Error while parsing the slip color palette")
		}

		/**
		 * Parses a string into a [SlipColorPalette] object.
		 * Throws an exception if the string is not valid.
		 */
		fun fromString(string: String): SlipColorPalette = try {
			SlipColorPalette().apply {
				string.split(",").forEach {
					// the string is in the format "#RRGGBB"
					add(it.substring(1).toInt(16))
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			throw Exception("Error while parsing the slip color palette")
		}
	}

	override fun toString(): String {
		var string = ""
		for (color in this) {
			string += "#${color.toString(16)},"
		}
		return string.removeSuffix(",")
	}
}