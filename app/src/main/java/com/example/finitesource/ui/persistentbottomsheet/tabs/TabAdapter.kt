package com.example.finitesource.ui.persistentbottomsheet.tabs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.finitesource.data.local.Products
import com.example.finitesource.states.UiState

/**
 * This class is responsible for managing the fragments displayed within a [ViewPager2]
 * to show different earthquake-related products based on their availability and resource status.
 */
class TabAdapter(
	fragmentActivity: FragmentActivity,
	private val state: UiState
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
//        return when (resource) {
//            // if the resource is loading or selected, return the loading fragment
//            is Resource.Loading, is Resource.Selected -> EventLoadingFragment()
//            // if the resource is error, return the error fragment
//            is Resource.Error -> EventErrorFragment()
//            // if the resource is loaded and the product is available, return the product fragment
//            is Resource.Loaded -> {
//                if (product in resource.data.availableProducts)
//                    product.newFragmentInstance()
//                // if the product is not available, return the not available fragment
//                else
//                    ProductNotAvailableFragment(product)
//            }
//        }
		return Fragment()   // TODO
	}
}
