package it.ingv.finitesource.ui.persistentbottomsheet

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import it.ingv.finitesource.data.local.Products
import it.ingv.finitesource.databinding.PersistentBottomSheetBinding
import it.ingv.finitesource.formatDateTime
import it.ingv.finitesource.formatDepth
import it.ingv.finitesource.formatMagnitude
import it.ingv.finitesource.getStatusBarHeight
import it.ingv.finitesource.ui.MainActivity
import it.ingv.finitesource.ui.persistentbottomsheet.behavior.BottomSheetVP2Helper
import it.ingv.finitesource.ui.persistentbottomsheet.behavior.ViewPagerBottomSheetBehavior
import it.ingv.finitesource.ui.persistentbottomsheet.tabs.TabAdapter
import it.ingv.finitesource.viewmodels.EarthquakesViewModel
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Custom layout class that represents a persistent bottom sheet with expand and collapse behavior.
 * It utilizes [ViewPagerBottomSheetBehavior] for controlling the bottom sheet's behavior.
 *
 * See [ExViewPagerBottomSheetBehavior](https://github.com/xcc3641/ExViewPagerBottomSheet).
 */
class PersistentBottomSheetLayout(context: Context, attr: AttributeSet) :
	LinearLayout(context, attr) {

	lateinit var bottomSheetBehavior: ViewPagerBottomSheetBehavior<PersistentBottomSheetLayout>

	lateinit var earthquakesViewModel: EarthquakesViewModel

	// The saved state of the bottom sheet, initially set to STATE_COLLAPSED
	private var savedState = ViewPagerBottomSheetBehavior.STATE_COLLAPSED

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()

		// Inflate the layout using data binding
		val binding = PersistentBottomSheetBinding.inflate(
			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
			this,
			true
		)

		// Setup ViewPager
		BottomSheetVP2Helper.setupViewPager(binding.viewPager)

		// listen to the selected event
		earthquakesViewModel.uiState.observe(context as LifecycleOwner) {
			if (it.selectedEarthquake != null) {
				// Initialize the tab layout and view pager
				binding.viewPager.adapter = TabAdapter(
					context as MainActivity,
					it
				)
				TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
					tab.text =
						context.resources.getString(Products.entries[position].tabNameId)
							.uppercase()
				}.attach()
				// set the data to the views
				binding.eventName.text = it.selectedEarthquake.name
				binding.eventMagnitude.text =
					formatMagnitude(context, it.selectedEarthquake.magnitude)
				binding.eventDate.text = formatDateTime(it.selectedEarthquake.date.time)
				binding.eventDepth.text = formatDepth(context, it.selectedEarthquake.depth)
			}
		}

		// Initialize the bottom sheet
		init()
	}


	// Initialize the bottom sheet behavior and set up event listeners
	private fun init() {
		orientation = VERTICAL
		setPadding(paddingLeft, paddingTop, paddingRight, getStatusBarHeight(resources))

		// Listen for a specific event and trigger bottom sheet minimization
//		EventBus.observe(context as LifecycleOwner) {
//			when (it.type) {
//				TOOLBAR_DOWN_ARROW_CLICKED -> bottomSheetBehavior.state =
//					ViewPagerBottomSheetBehavior.STATE_COLLAPSED
//			}
//		}
	}

	/**
	 * Check if the bottom sheet is expanded.
	 */
	fun isExpanded() = bottomSheetBehavior.state == ViewPagerBottomSheetBehavior.STATE_EXPANDED

	/**
	 * Expand the bottom sheet.
	 */
	fun expand() {
		bottomSheetBehavior.state = ViewPagerBottomSheetBehavior.STATE_EXPANDED
	}

	/**
	 * Check if the bottom sheet is collapsed.
	 */
	fun isCollapsed() = bottomSheetBehavior.state == ViewPagerBottomSheetBehavior.STATE_COLLAPSED

	/**
	 * Collapse the bottom sheet.
	 */
	fun collapse() {
		bottomSheetBehavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
	}

	// Save the state of the view hierarchy
	override fun onSaveInstanceState(): Parcelable {
		val bundle = Bundle()
		bundle.putInt(BOTTOM_SHEET_STATE_KEY, bottomSheetBehavior.state)
		bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
		return bundle
	}

	// Restore the state of the view hierarchy
	override fun onRestoreInstanceState(state: Parcelable?) {
		val superState = if (state is Bundle) {
			savedState = state.getInt(BOTTOM_SHEET_STATE_KEY)
			state.getParcelable(SUPER_STATE_KEY)
		} else {
			state
		}
		super.onRestoreInstanceState(superState)
	}

	// Constants
	private companion object {
		private const val BOTTOM_SHEET_STATE_KEY = "bottomSheetState"
		private const val SUPER_STATE_KEY = "superState"
		private const val BOTTOM_SHEET_MAXIMIZED_SLIDE_OFFSET = 0.93f
	}
}
