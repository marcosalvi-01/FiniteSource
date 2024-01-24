package com.example.finitesource.ui.persistentbottomsheet.tabs.scenarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finitesource.data.local.ProductFragment
import com.example.finitesource.databinding.FragmentScenariosBinding

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

	// Lazily initialize the ViewModel for this fragment
//	private val viewModel: ScenariosFragmentViewModel by lazy {
//		ViewModelProvider(this)[ScenariosFragmentViewModel::class.java]
//	}

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
	}
}
