package com.example.finitesource.data.earthquake

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finitesource.offsetDateTimeToCalendar
import org.openapitools.client.models.FiniteSourceAppAppJsonGet200ResponseInner
import java.util.Calendar

@Entity
data class Earthquake(
	@PrimaryKey val id: String,
	val name: String,
	val date: Calendar,    // instead of Date
	val magnitude: Double,
	val depth: Double,
	val latitude: Double,
	val longitude: Double,
//	val boundingBox: List<Double>,    // TODO see if it is better to use the osmdroid one
	val finiteSourceLastUpdate: Calendar? = null,
	@Embedded val details: EarthquakeDetails? = null,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Earthquake) return false

		return id == other.id
	}

	override fun hashCode(): Int {
		return id.hashCode()
	}


}

// TODO see why the fields of FiniteSourceAppAppJsonGet200ResponseInner are nullable they should not be nullable
// TODO find a better way to handle the nullability of the fields
fun toEarthquake(response: FiniteSourceAppAppJsonGet200ResponseInner): Earthquake? {
	return try {
		Earthquake(
			id = response.idEvent!!,
			name = response.name!!,
			date = offsetDateTimeToCalendar(response.occurringTime)!!,
			magnitude = response.magnitude!!,
			depth = response.depth!!,
			latitude = response.latitude!!,
			longitude = response.longitude!!,
			finiteSourceLastUpdate = offsetDateTimeToCalendar(response.finiteSourceLastUpdated),
			details = null,
		)
	} catch (e: Exception) {
		null
	}
}