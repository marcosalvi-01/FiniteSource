package com.example.finitesource.ui.persistentbottomsheet.tabs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.finitesource.data.local.Products
import com.example.finitesource.states.UiState
import com.example.finitesource.ui.persistentbottomsheet.tabs.fragmentstates.EventErrorFragment
import com.example.finitesource.ui.persistentbottomsheet.tabs.fragmentstates.EventLoadingFragment
import com.example.finitesource.ui.persistentbottomsheet.tabs.fragmentstates.ProductNotAvailableFragment

/**
 * This class is responsible for managing the fragments displayed within a [ViewPager2]
 * to show different earthquake-related products based on their availability and resource status.
 */
class TabAdapter(
	fragmentActivity: FragmentActivity,
	private val uiState: UiState
) : FragmentStateAdapter(fragmentActivity) {

	override fun getItemCount(): Int {
		return Products.entries.size
	}

	override fun createFragment(position: Int): Fragment {
		// Get the product at the given position
		val product = Products.entries[position]
		// Return the appropriate fragment based on the product
		return getActiveFragment(product)
	}

	/**
	 * Determines the appropriate fragment to be displayed based on the given product and resource status.
	 */
	private fun getActiveFragment(product: Products): Fragment {
		// based on the state, return the appropriate fragment
		// if there is an error while loading, show the error fragment
		if (uiState.loadingState.errorWhileLoading)
			return EventErrorFragment()
		// if there is no selected earthquake, show the loading fragment
		if (uiState.loadingState.loading)
			return EventLoadingFragment()
		val selectedEarthquake = uiState.selectedEarthquake
		val selectedFocalPlane = uiState.selectedFocalPlane
		// if the product is available, return the fragment for the product
		if (product in (selectedEarthquake?.details?.getFocalPlane(selectedFocalPlane)?.availableProducts
				?: emptyList())
		)
			return product.newFragmentInstance()
		// if the product is not available, return the fragment for the product not available
		return ProductNotAvailableFragment(product)
	}
}
