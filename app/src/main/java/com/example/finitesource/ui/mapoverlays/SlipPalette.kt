package com.example.finitesource.ui.mapoverlays

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import com.example.finitesource.R
import java.util.Locale

/**
 * A layout that is a vertical color palette. It displays the max and min slip values.
 *
 * It is basically a layout with a lot of 1dp height views with different colors.
 */
class SlipPalette(context: Context) : RelativeLayout(context) {
	// the corner radius of the background needs to be half the width of the palette

	// Max slip property with setter to update the max slip text
	var maxSlip = 0.0
		set(value) {
			field = value
			val maxSlipText = getChildAt(0) as TextView
			maxSlipText.text = context.getString(
				R.string.slip_palette_max_depth,
				String.format(Locale.US, "%.1f", maxSlip)
			)
		}

	init {
		// Do nothing on click, consume the click event
		setOnClickListener { }

		id = R.id.slip_palette
		visibility = View.GONE

		// Get width and background drawable
		val width = context.resources.getDimension(R.dimen.slip_palette_width).toInt()
		val drawable = ResourcesCompat.getDrawable(
			resources,
			R.drawable.slip_palette_border,
			getContext().theme
		)

		// Set layout params
		layoutParams = CoordinatorLayout.LayoutParams(
			CoordinatorLayout.LayoutParams.WRAP_CONTENT,
			CoordinatorLayout.LayoutParams.WRAP_CONTENT
		).apply {
			anchorId = R.id.compass_button_container
			gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
			anchorGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
		}

		// Create and add max slip text
		val slipText = makeMaxSlipText(context, maxSlip)
		this.addView(slipText)

		// Create and add min slip text
		val minSlipText = makeMinSlipText(context)
		this.addView(minSlipText)

		// Create a container layout for the border
		val borderContainer = LinearLayout(context).apply {
			id = generateViewId()
			orientation = LinearLayout.VERTICAL
			val borderParams = LayoutParams(width, LayoutParams.WRAP_CONTENT)
			// Move the border container under the text
			borderParams.addRule(BELOW, slipText.id)
			// Center the border container horizontally
			borderParams.addRule(CENTER_HORIZONTAL)
			layoutParams = borderParams
			// Set the background drawable with rounded corners
			background = drawable
			// Set the padding of the border container
			val padding = context.resources.getDimension(R.dimen.stroke_width).toInt()
			setPadding(padding, padding, padding, padding)
		}

		// Create a container for the colors
		val container = LinearLayout(context).apply {
			orientation = LinearLayout.VERTICAL
			layoutParams =
				LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
				)
			// Set the background drawable with rounded corners
			background = ResourcesCompat.getDrawable(
				resources,
				R.drawable.slip_palette_background,
				getContext().theme
			)
		}

		// Clip the corners of the border container
		clipToOutline = true
		container.clipToOutline = true
		borderContainer.clipToOutline = true

		// Get the color array
		val colorArray = this.resources.getIntArray(R.array.color_palette)

		// Set the view height for each color
		val viewHeight = context.resources.getDimension(R.dimen.slip_palette_color_height).toInt()

		// Add the colors to the palette
		for (i in colorArray.indices.reversed()) {
			// Add the color view to the parent layout
			container.addView(View(context).apply {
				// Set the color
				setBackgroundColor(colorArray[i])
				// Set the view height
				layoutParams =
					FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, viewHeight)
			})
		}

		// Add the container to the border container
		borderContainer.addView(container)

		// Move the min slip text to the bottom of the palette
		val minSlipParams = minSlipText.layoutParams as LayoutParams
		minSlipParams.addRule(BELOW, borderContainer.id)

		// Add the container to the palette
		this.addView(borderContainer)
	}

	private fun makeMaxSlipText(context: Context, maxSlip: Double): TextView {
		return TextView(context).apply {
			// Set the id
			id = generateViewId()
			// Set the text to maxSlip up to one decimal place
			// Format the maxSlip value to display up to the first decimal place
			text = context.getString(
				R.string.slip_palette_max_depth,
				String.format(Locale.US, "%.1f", maxSlip)
			)
			// Set the text color
			setTextColor(Color.BLACK)
			// Set the text size
			setTextSize(
				TypedValue.COMPLEX_UNIT_PX,
				context.resources.getDimensionPixelSize(R.dimen.slip_palette_text_size).toFloat()
			)
			// Set the text alignment
			textAlignment = TEXT_ALIGNMENT_CENTER
			// Set the layout params
			layoutParams =
				LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
		}
	}

	private fun makeMinSlipText(context: Context): TextView {
		return TextView(context).apply {
			// Set the id
			id = generateViewId()
			// Set the text to the min slip string
			text = context.getString(R.string.slip_palette_min_depth)
			// Set the text color
			setTextColor(Color.BLACK)
			// Set the text size
			setTextSize(
				TypedValue.COMPLEX_UNIT_PX,
				context.resources.getDimensionPixelSize(R.dimen.slip_palette_text_size).toFloat()
			)
			// Set the text alignment
			textAlignment = TEXT_ALIGNMENT_CENTER
			// Set the layout params
			layoutParams =
				LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
		}
	}

	override fun onSaveInstanceState(): Parcelable {
		val bundle = Bundle()
		bundle.putInt(VISIBILITY_KEY, visibility)
		bundle.putDouble(MAX_SLIP_KEY, maxSlip)
		bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
		return bundle
	}

	override fun onRestoreInstanceState(state: Parcelable?) {
		var superState = state
		if (state is Bundle) {
			visibility = state.getInt(VISIBILITY_KEY)
			maxSlip = state.getDouble(MAX_SLIP_KEY)
			superState = state.getParcelable(SUPER_STATE_KEY)
		}
		super.onRestoreInstanceState(superState)
	}

	companion object {
		private const val VISIBILITY_KEY = "visibilityKey"
		private const val SUPER_STATE_KEY = "superStateKey"
		private const val MAX_SLIP_KEY = "maxSlipKey"
	}
}
