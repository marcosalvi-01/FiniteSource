package com.example.finitesource.data.local.earthquake

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlane
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.offsetDateTimeToCalendar
import org.openapitools.client.models.Event
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
) {
    @Ignore
    var details: EarthquakeDetails? = null
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

    /**
     * This function is used to count the number of focal planes associated with an earthquake.
     * It checks if the earthquake details contain focal plane 1 (fp1) and focal plane 2 (fp2).
     * If only fp1 is present, it returns 1.
     * If only fp2 is present, it returns 2.
     * If both fp1 and fp2 are present, it returns 0.
     * If neither fp1 nor fp2 are present, it returns null.
     *
     * @return The count of focal planes associated with the earthquake or null if no focal planes are present.
     */
    fun focalPlaneCount(): Int? {
        var count: Int? = null
        if (details?.fp1 != null) count = 1
        if (details?.fp2 != null) count = if (count == 1) 0 else 2
        return count
    }

    /**
     * This function is used to get the maximum slip of a focal plane associated with an earthquake.
     * It takes a focal plane type (FP1 or FP2) as an argument.
     * If the earthquake details contain the given focal plane type and it has a finite source with a max slip value, it returns that value.
     * If the earthquake details do not contain the given focal plane type or it does not have a finite source with a max slip value, it returns 0.0.
     *
     * @param focalPlaneType The type of the focal plane (FP1 or FP2).
     * @return The maximum slip of the focal plane or 0.0 if the focal plane or its max slip value does not exist.
     */
    fun focalPlaneMaxSlip(focalPlaneType: FocalPlaneType): Double {
        return when (focalPlaneType) {
            FocalPlaneType.FP1 -> details?.fp1?.finiteSource?.sourceJson?.maxSlip ?: 0.0
            FocalPlaneType.FP2 -> details?.fp2?.finiteSource?.sourceJson?.maxSlip ?: 0.0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Earthquake) return false
        // TODO consider maybe adding a last update field to the earthquake class
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

// TODO see why the fields of FiniteSourceAppAppJsonGet200ResponseInner are nullable they should not be nullable
// TODO find a better way to handle the nullability of the fields
fun toEarthquake(event: Event): Earthquake? {
    return try {
        Earthquake(
            id = event.idEvent,
            name = event.name,
            date = offsetDateTimeToCalendar(event.occurringTime)!!,
            magnitude = (event.magnitude * 10).roundToInt() / 10.0,
            depth = event.depth.toDouble(),
            latitude = event.latitude.toDouble(),
            longitude = event.longitude.toDouble(),
            finiteSourceLastUpdate = offsetDateTimeToCalendar(event.finiteSource?.maxBy { it.processingTime!! }?.processingTime),
            boundingBox = normalizeBoundingBox(event.boundingBox.let {
                listOf(
                    it.south!!.toDouble(),
                    it.west!!.toDouble(),
                    it.north!!.toDouble(),
                    it.east!!.toDouble()
                )
            }),
        )
//		Earthquake(
//			id = response.idEvent!!,
//			name = response.name!!,
//			date = offsetDateTimeToCalendar(response.occurringTime)!!,
//			magnitude = (response.magnitude!! * 10).roundToInt() / 10.0,
//			depth = response.depth!!,
//			latitude = response.latitude!!,
//			longitude = response.longitude!!,
//			finiteSourceLastUpdate = offsetDateTimeToCalendar(response.finiteSourceLastUpdated),
//			boundingBox = normalizeBoundingBox(response.boundingBox!!),
//			details = null,
//		)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun normalizeBoundingBox(boundingBox: List<Double>): BoundingBox {
    val (south, west, north, east) = boundingBox
    // zoom out the bounding box
    val eastDelta = 0.25
    val westDelta = 0.25
    val northDelta = 0.3
    val southDelta = 0.3
    val width = east - west
    val height = north - south
    return BoundingBox(
        north + (northDelta * height),
        east + (eastDelta * width),
        south - (southDelta * height),
        west - (westDelta * width)
    )
}