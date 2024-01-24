package com.example.finitesource.ui.persistentbottomsheet.tabs.finitesource

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finitesource.data.local.ProductFragment
import com.example.finitesource.databinding.FragmentFiniteSourceBinding

/**
 * Corresponds to the [FiniteSource] product.
 *
 * This fragment displays the [FiniteSource]'s data of a [FocalPlane] of the selected [EarthquakeEvent].
 */
class FiniteSourceFragment : ProductFragment() {
	// Lazy initialization of binding using View Binding
	private val binding: FragmentFiniteSourceBinding by lazy {
		FragmentFiniteSourceBinding.inflate(layoutInflater)
	}

//    // Lazy initialization of ViewModel using ViewModelProvider
//    private val viewModel: FiniteSourceFragmentViewModel by lazy {
//        ViewModelProvider(this)[FiniteSourceFragmentViewModel::class.java]
//    }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		// Observe changes in the selectedFocalPlaneLiveData
//        viewModel.selectedFocalPlaneLiveData.observe(viewLifecycleOwner) { focalPlane ->
//            focalPlane?.finiteSource?.let { finiteSource ->
//                // TODO use data binding
//                // Update the UI
//                binding.inversionDescription.text = finiteSource.inversionDescription
//                binding.mainInversionMap.imageUrl = finiteSource.mainInversionMapImageUrl
//                binding.slipDistribution.imageUrl = finiteSource.slipDistributionImageUrl
//                binding.resultDescription.text =
//                    Html.fromHtml(finiteSource.resultDescription, Html.FROM_HTML_MODE_COMPACT)
//                // Download the zip file when the button is clicked
//                binding.downloadZipButton.setOnClickListener {
//                    downloadZip("${finiteSource.url}/$ZIP_FILE_NAME")
//                }
//
//                // Set the titles for the ImageTextViews
//                setTitleId(product.tabNameId, binding.mainInversionMap, binding.slipDistribution)
//            }
//        }
	}

	// Download the zip file from the given url by calling the downloadZip()
	// method in the viewModel
	private fun downloadZip(zipUrl: String) {
		// Update UI before downloading
		binding.downloadZipButton.visibility = View.GONE
		binding.downloadZipProgressBar.visibility = View.VISIBLE

		// Perform download in IO dispatcher
//        viewModel.viewModelScope.launch(Dispatchers.IO) {
//            val downloadSuccessful = viewModel.downloadZip(zipUrl)
//
//            // Update UI on the main thread after download
//            withContext(Dispatchers.Main) {
//                // Restore UI visibility
//                binding.downloadZipProgressBar.visibility = View.GONE
//                binding.downloadZipButton.visibility = View.VISIBLE
//
//                // Show appropriate toast message
//                val messageResId = if (downloadSuccessful)
//                    R.string.download_completed
//                else
//                    R.string.download_failed
//
//                Toast.makeText(
//                    requireContext(),
//                    requireContext().getString(messageResId),
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
	}
}
