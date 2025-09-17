package it.ingv.finitesource.ui.mapview

import org.osmdroid.views.overlay.Polygon

/**
 * This class represent a polygon with a slip and a rake.
 *
 * It extends the [Polygon] class from the osmdroid library.
 */
class CustomPolygon(val slip: Double, val rake: Double) : Polygon() {
	// TODO add an infowindow to show the slip and rake
	override fun equals(other: Any?): Boolean {
		if (other == null || other !is CustomPolygon)
			return false
		return slip == other.slip && rake == other.rake && actualPoints == other.actualPoints
	}

	override fun hashCode(): Int {
		var result = slip.hashCode()
		result = 31 * result + rake.hashCode()
		return result
	}
}