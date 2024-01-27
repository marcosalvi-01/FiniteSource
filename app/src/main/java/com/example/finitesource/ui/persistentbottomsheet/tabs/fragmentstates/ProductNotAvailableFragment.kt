package com.example.finitesource.ui.persistentbottomsheet.tabs.fragmentstates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.finitesource.data.local.Products
import com.example.finitesource.databinding.FragmentProductNotAvailableBinding

/**
 * Fragment to display when the product is not available for this event.
 *
 * It is displayed instead of the correct fragment for that tab.
 *
 * See [ProductFragment].
 */
class ProductNotAvailableFragment(private val product: Products) : Fragment() {
	// lazy initialization of the binding
	private val binding: FragmentProductNotAvailableBinding by lazy {
		FragmentProductNotAvailableBinding.inflate(layoutInflater)
	}

	// Add a default (no-argument) constructor needed for the fragment manager to re-instantiate
	// using FINITE_SOURCE as the default product.
	constructor() : this(Products.FINITE_SOURCE)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		// set the text
		binding.textViewProductNotAvailable.text = resources.getString(product.notAvailableTextId)
	}
}