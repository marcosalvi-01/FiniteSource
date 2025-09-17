package it.ingv.finitesource.ui.persistentbottomsheet.tabs.footprints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import it.ingv.finitesource.data.local.ProductFragment
import it.ingv.finitesource.databinding.FragmentFootprintBinding
import it.ingv.finitesource.setTitleId
import it.ingv.finitesource.viewmodels.EarthquakesViewModel

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

	// initialize the view model
	private val earthquakesViewModel: EarthquakesViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		// Observe the UI state of the EarthquakesViewModel
		earthquakesViewModel.uiState.observe(viewLifecycleOwner) {
			// Get the selected earthquake event from the UI state
			val event = it.selectedEarthquake
			// Check if the selected earthquake event is not null
			if (event != null) {
				// Get the footprints details of the selected earthquake event
				val footprints = event.details?.footprints
				// Check if the footprints details are not null
				if (footprints != null) {
					// Set the description text of the binding with the description of the footprints
					binding.description.text = footprints.description
					// Set the image URL of the sentinel footprint in the binding with the image URL of the footprints
					binding.sentinelFootprint.imageUrl = footprints.imageUrl
					// Set the title ID of the product tab name in the binding with the sentinel footprint
					setTitleId(product.tabNameId, binding.sentinelFootprint)
				}
			}
		}
	}
}
