package com.example.finitesource.ui.persistentbottomsheet.tabs.footprints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finitesource.data.local.ProductFragment
import com.example.finitesource.databinding.FragmentFootprintBinding

/**
 * Corresponds to the [Footprint] product.
 *
 * This fragment displays the [Footprint]'s data of the selected [EarthquakeEvent].
 */
class FootprintsFragment : ProductFragment() {
	// Lazily initialize the binding using FragmentFootprintBinding
	private val binding: FragmentFootprintBinding by lazy {
		FragmentFootprintBinding.inflate(layoutInflater)
	}

	// Lazily initialize the view model using FootprintFragmentViewModel
//    private val viewModel: FootprintFragmentViewModel by lazy {
//        ViewModelProvider(this)[FootprintFragmentViewModel::class.java]
//    }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		// Observe the selected earthquake event data from the view model
//        viewModel.selectedEarthquakeEventLiveData.observe(viewLifecycleOwner) { earthquakeEvent ->
//            earthquakeEvent?.let {
//                // Set the description from the Footprint companion object
//                binding.description.text = Footprint.description
//
//                // Set the image URL from the earthquake event data
//                binding.sentinelFootprint.imageUrl = it.data?.footprint?.imagePath
//
//                // Set the title of the ImageTextView
//                setTitleId(product.tabNameId, binding.sentinelFootprint)
//            }
//        }
	}
}
