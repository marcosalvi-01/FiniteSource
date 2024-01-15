package com.example.finitesource.ui.mapoverlays

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar

/**
 * Custom SeekBar widget designed for controlling alpha values of the slip distribution showed on
 * the map.
 *
 * This widget extends the [AppCompatSeekBar] to add the functionality of saving and restoring
 * its visibility state during device rotations.
 */
class SlipAlphaSlider(context: Context, attributeSet: AttributeSet) :
	AppCompatSeekBar(context, attributeSet) {

	// Override onSaveInstanceState to save the visibility state along with super state
	override fun onSaveInstanceState(): Parcelable {
		val bundle = Bundle()
		bundle.putInt(SLIP_ALPHA_SLIDER_VISIBILITY, visibility)
		bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
		return bundle
	}

	// Override onRestoreInstanceState to restore the visibility state and super state
	override fun onRestoreInstanceState(state: Parcelable?) {
		var superState = state
		if (state is Bundle) {
			visibility = state.getInt(SLIP_ALPHA_SLIDER_VISIBILITY)
			superState = state.getParcelable(SUPER_STATE_KEY)
		}
		super.onRestoreInstanceState(superState)
	}

	// Constants for key names used in Bundle
	private companion object {
		private const val SUPER_STATE_KEY = "superStateKey"
		private const val SLIP_ALPHA_SLIDER_VISIBILITY = "slipAlphaSliderVisibility"
	}
}
