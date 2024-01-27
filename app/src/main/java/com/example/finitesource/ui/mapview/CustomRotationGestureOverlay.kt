package com.example.finitesource.ui.mapview

import android.view.MotionEvent
import android.widget.ImageButton
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import kotlin.math.abs

/**
 * This class extends the RotationGestureOverlay and
 * provides custom rotation behavior for a [CustomMapView].
 */
class CustomRotationGestureOverlay(
	private val mapView: CustomMapView,
	private val compassButton: ImageButton,
) :
	RotationGestureOverlay(mapView) {
	private var deltaAngle = 0f // Stores the cumulative rotation angle change.
	private var currentAngle = 0f // Stores the current total rotation angle.

	init {
		isEnabled = true // Enable rotation gestures by default.
	}

	/**
	 * Callback when the rotation gesture is detected.
	 */
	override fun onRotate(deltaAngle: Float) {
		// Apply a deadzone to prevent minor angle changes from triggering rotation.
		this.deltaAngle += deltaAngle
		if (!(abs(this.deltaAngle) < ROTATION_DEADZONE && currentAngle == 0f)) {
			// increment the angle for the rotation
			currentAngle += deltaAngle
			// set the rotation of the map
			mapView.mapOrientation = mapView.mapOrientation + currentAngle
			// set the rotation of the compass button
			compassButton.rotation = mapView.mapOrientation
		}
	}

	/**
	 * Callback for touch events.
	 */
	override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
		// Reset angles when the gesture is finished.
		if (event.action == MotionEvent.ACTION_UP) {
			currentAngle = 0f
			deltaAngle = 0f
		}
		return super.onTouchEvent(event, mapView)
	}

	companion object {
		private const val ROTATION_DEADZONE =
			20f // Minimum rotation angle to trigger actual rotation.
	}
}
