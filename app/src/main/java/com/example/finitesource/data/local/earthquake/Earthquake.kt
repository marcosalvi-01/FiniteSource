package com.example.finitesource.data.local.earthquake

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlane
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.offsetDateTimeToCalendar
import org.openapitools.client.models.FiniteSourceAppAppJsonGet200ResponseInner
import org.osmdroid.util.BoundingBox
import java.util.Calendar
import kotlin.math.roundToInt

@Entity
data class Earthquake(
	@PrimaryKey val id: String,
	val name: String,
	val date: Calendar,    // instead of Date
	val magnitude: Double,
	val depth: Double,
	val latitude: Double,
	val longitude: Double,
	val boundingBox: BoundingBox,
	var finiteSourceLastUpdate: Calendar? = null,
	@Embedded var details: EarthquakeDetails? = null,
) {
	/**
	 * Returns the focal plane of the given type.
	 * Returns null if the earthquake doesn't have the details or if the focal plane doesn't exist.
	 */
	fun getFocalPlane(focalPlaneType: FocalPlaneType): FocalPlane? {
		return when (focalPlaneType) {
			FocalPlaneType.FP1 -> details?.fp1
			FocalPlaneType.FP2 -> details?.fp2
		}
	}

	fun hasFiniteSource(): Boolean = finiteSourceLastUpdate != null

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
			magnitude = (response.magnitude!! * 10).roundToInt() / 10.0,
			depth = response.depth!!,
			latitude = response.latitude!!,
			longitude = response.longitude!!,
			finiteSourceLastUpdate = offsetDateTimeToCalendar(response.finiteSourceLastUpdated),
			boundingBox = BoundingBox(
				// South, West, North, East
				response.boundingBox!![2],
				response.boundingBox[3],
				response.boundingBox[0],
				response.boundingBox[1]
			),
			details = null,
		)
	} catch (e: Exception) {
		null
	}
}