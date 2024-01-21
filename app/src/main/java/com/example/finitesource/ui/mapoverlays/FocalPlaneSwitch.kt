package com.example.finitesource.ui.mapoverlays

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.finitesource.R
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * Custom Switch component to switch between focal planes.
 * This switch is vertically rotated and positioned using a custom
 * behavior to stay aligned with a dependency view.
 */
// this class is a mess, it should be refactored
// TODO find a better way to show that the focal plane changed if the event doesn't have a finite source
// TODO there is a bug right now where the switch's position is sometimes wrong, but moving the map fixes it
class FocalPlaneSwitch(context: Context, attributeSet: AttributeSet) :
	SwitchMaterial(context, attributeSet) {

	var anchorView: View? = null

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		isChecked = true // Select fp1 by default
		// Set switch graphics resources
		setThumbResource(R.drawable.focal_plane_switch_thumb_selector)
		setTrackResource(R.drawable.focal_plane_switch_track_selector)
		rotation = -90f // Rotate the switch
		pivotX = 0f
		pivotY = 0f
		minHeight = 0 // Remove default margins and paddings
		minWidth = 0
		minimumHeight = 0
		minimumWidth = 0
		// Set layout parameters
		val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
		// Set the custom behavior to consider the rotation
		layoutParams.behavior =
			FocalPlaneSwitchBehavior(resources.getDimension(R.dimen.map_overlay_icon_vertical_margin))
		setLayoutParams(layoutParams)
	}

	override fun onSaveInstanceState(): Parcelable {
		val bundle = Bundle()
		bundle.putInt(SWITCH_VISIBILITY_KEY, visibility)
		bundle.putBoolean(SWITCH_CHECKED_KEY, isChecked)
		bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
		return bundle
	}

	override fun onRestoreInstanceState(state: Parcelable?) {
		var superState = state
		if (state is Bundle) {
			visibility = state.getInt(SWITCH_VISIBILITY_KEY)
			isChecked = state.getBoolean(SWITCH_CHECKED_KEY)
			superState = state.getParcelable(SUPER_STATE_KEY)
		}
		super.onRestoreInstanceState(superState)
	}

	companion object {
		private const val SUPER_STATE_KEY = "superStateKey"
		private const val SWITCH_VISIBILITY_KEY = "switchVisibilityKey"
		private const val SWITCH_CHECKED_KEY = "switchCheckedKey"
	}
}

/**
 * Custom behavior for positioning the [FocalPlaneSwitch] relative to a dependency view.
 */
class FocalPlaneSwitchBehavior(private val topMargin: Float) :
	CoordinatorLayout.Behavior<FocalPlaneSwitch>() {

	override fun layoutDependsOn(
		parent: CoordinatorLayout,
		child: FocalPlaneSwitch,
		dependency: View
	): Boolean {
		// Check if the dependency is the desired view (search bar container)
		if (dependency.id == R.id.slip_alpha_slider_container) {
			child.anchorView = dependency
			return true
		}
		return false
	}

	override fun onLayoutChild(
		parent: CoordinatorLayout,
		child: FocalPlaneSwitch,
		layoutDirection: Int
	): Boolean {
		val dependencyView = child.anchorView // Get the dependency view

		if (dependencyView != null && dependencyView.visibility != View.VISIBLE) {
			// Update the position of the child view based on the dependency view's visibility
			child.y = dependencyView.y + child.width
		} else {
			// Update the position of the child view based on the dependency view's position
			if (dependencyView != null)
			// the width of the child is the height because it is rotated
				child.y = dependencyView.y + dependencyView.height + child.width + topMargin
			else
				child.y = 0f
		}

		return super.onLayoutChild(parent, child, layoutDirection)
	}
}