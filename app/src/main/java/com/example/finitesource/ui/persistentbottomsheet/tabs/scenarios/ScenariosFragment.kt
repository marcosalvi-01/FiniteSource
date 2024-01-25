package com.example.finitesource.ui.persistentbottomsheet.tabs.scenarios

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finitesource.data.local.ProductFragment
import com.example.finitesource.data.local.earthquake.focalplane.Scenarios
import com.example.finitesource.databinding.FragmentScenariosBinding
import com.example.finitesource.viewmodels.EarthquakesViewModel

/**
 * Corresponds to the [Scenarios] product.
 *
 * This fragment displays the [Scenarios] of a [FocalPlane] of the selected [EarthquakeEvent].
 *
 * It uses a [RecyclerView] with a [ExpandableItemAdapter] to display expandable items.
 */
class ScenariosFragment : ProductFragment() {
	// Lazily initialize the view binding for the fragment
	private val binding: FragmentScenariosBinding by lazy {
		FragmentScenariosBinding.inflate(layoutInflater)
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
		// Observe the selected focal plane and update the UI when it changes
//		viewModel.selectedFocalPlaneLiveData.observe(viewLifecycleOwner) { focalPlane ->
//			// Update the UI only if the focal plane is not null and the scenarios are not null
//			focalPlane?.scenarios?.let {
//				binding.recyclerView.adapter = ExpandableItemAdapter(it.scenarios)
//				binding.recyclerView.layoutManager = LinearLayoutManager(context)
//				binding.globalDescription.text =
//					Html.fromHtml(Scenarios.description, Html.FROM_HTML_MODE_COMPACT)
//			}
//		}

		// Observe the UI state of the EarthquakesViewModel
		earthquakesViewModel.uiState.observe(viewLifecycleOwner) {
			// Get the selected earthquake event from the UI state
			val event = it.selectedEarthquake
			val focalPlaneType = it.selectedFocalPlane
			// Check if the selected earthquake event is not null
			if (event != null && focalPlaneType != null) {
				// Get the scenarios details of the selected earthquake event
				val scenarios = event.getFocalPlane(focalPlaneType)?.scenarios
				// Check if the scenarios details are not null
				if (scenarios != null) {
					binding.recyclerView.adapter = ExpandableItemAdapter(scenarios.scenarios)
					binding.recyclerView.layoutManager = LinearLayoutManager(context)
					binding.globalDescription.text =
						Html.fromHtml(scenarios.description, Html.FROM_HTML_MODE_COMPACT)
				}
			}
		}
	}
}
