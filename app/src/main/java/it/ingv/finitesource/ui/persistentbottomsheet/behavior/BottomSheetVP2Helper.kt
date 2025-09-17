package it.ingv.finitesource.ui.persistentbottomsheet.behavior

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager2.widget.ViewPager2

/**
 * Utility class to assist with setting up [ViewPager2] within a [ViewPagerBottomSheetBehavior].
 */
object BottomSheetVP2Helper {

	/**
	 * Sets up a [ViewPager2] inside a [ViewPagerBottomSheetBehavior].
	 */
	fun setupViewPager(vp: ViewPager2) {
		// Find the parent BottomSheet view and register a callback for ViewPager2 changes.
		findBottomSheetParent(vp)?.also {
			vp.registerOnPageChangeCallback(
				BottomSheetViewPagerListener(
					vp,
					ViewPagerBottomSheetBehavior.from(it)
				)
			)
		}
	}

	/**
	 * Listener for [ViewPager2] changes that refreshes the scrolling behavior of the [ViewPagerBottomSheetBehavior].
	 */
	private class BottomSheetViewPagerListener(
		private val vp: ViewPager2,
		private val behavior: ViewPagerBottomSheetBehavior<View>
	) : ViewPager2.OnPageChangeCallback() {

		/**
		 * Called when a new page is selected in the [ViewPager2].
		 */
		override fun onPageSelected(position: Int) {
			// Post an action to refresh the scrolling behavior of the BottomSheet.
			vp.post(behavior::invalidateScrollingChild)
		}
	}

	/**
	 * Finds the parent [ViewPagerBottomSheetBehavior] view for a given child view.
	 */
	private fun findBottomSheetParent(view: View): View? {
		var current: View? = view
		// Traverse up the view hierarchy to find a parent with the required behavior.
		while (current != null) {
			val params = current.layoutParams
			if (params is CoordinatorLayout.LayoutParams && params.behavior is ViewPagerBottomSheetBehavior<*>) {
				return current
			}
			val parent = current.parent
			current = if (parent !is View) null else parent
		}
		return null
	}
}
