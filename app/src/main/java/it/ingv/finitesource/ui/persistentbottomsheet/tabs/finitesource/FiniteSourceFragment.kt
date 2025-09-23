package it.ingv.finitesource.ui.persistentbottomsheet.tabs.finitesource

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import it.ingv.finitesource.R
import it.ingv.finitesource.data.local.ProductFragment
import it.ingv.finitesource.databinding.FragmentFiniteSourceBinding
import it.ingv.finitesource.setTitleId
import it.ingv.finitesource.states.UiState
import it.ingv.finitesource.viewmodels.EarthquakesViewModel

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
		earthquakesViewModel.uiState.observe(viewLifecycleOwner) {
			val event = it.selectedEarthquake
			val focalPlaneType = it.selectedFocalPlane
			if (event != null && focalPlaneType != null) {
				val finiteSource = event.getFocalPlane(focalPlaneType)?.finiteSource
				if (finiteSource != null) {
					binding.inversionDescription.text = finiteSource.inversionDescription
					binding.mainInversionMap.imageUrl = finiteSource.mainInversionMapImageUrl
					binding.slipDistribution.imageUrl = finiteSource.slipDistributionImageUrl
					binding.resultDescription.text =
						Html.fromHtml(finiteSource.resultDescription, Html.FROM_HTML_MODE_COMPACT)
					binding.downloadZipButton.setOnClickListener {
						downloadZipButtonClicked(earthquakesViewModel.uiState.value)
					}
					// Set the titles for the ImageTextViews
					setTitleId(
						product.tabNameId,
						binding.mainInversionMap,
						binding.slipDistribution
					)
				}
			}
		}
	}

	/**
	 * Handles the click event for the download zip button.
	 *
	 * This function is triggered when the download zip button is clicked. It first checks if the uiState is null.
	 * If it is not null, it hides the download button and shows a progress bar to indicate that the download is in progress.
	 * It then calls the `downloadZipToFile` method in the `earthquakesViewModel` to start the download.
	 * The function observes the result of the download and updates the UI accordingly.
	 * If the download is successful, it shows a toast message indicating that the download is completed.
	 * If the download fails, it shows a toast message indicating that the download has failed.
	 *
	 * @param uiState The current state of the UI. It is used to get the selected earthquake and focal plane for the download.
	 */
	private fun downloadZipButtonClicked(uiState: UiState?) {
		// Check if the uiState is null
		if (uiState == null)
			return

		// Update UI before downloading
		binding.downloadZipButton.visibility = View.GONE
		binding.downloadZipProgressBar.visibility = View.VISIBLE

		// Start the download and listen for the result
		earthquakesViewModel.copyZipUrlToClipboard(
			uiState.selectedEarthquake!!,
			uiState.selectedFocalPlane!!,
			requireContext(),
		).observe(
			viewLifecycleOwner
		) {
			// it is a Boolean that indicates whether the download was successful or not
			// Update UI on the main thread after download
			binding.downloadZipProgressBar.visibility = View.GONE
			binding.downloadZipButton.visibility = View.VISIBLE

			// Show appropriate toast message
			val messageResId = if (it)
				R.string.download_completed
			else
				R.string.download_failed

			Toast.makeText(
				requireContext(),
				requireContext().getString(messageResId),
				Toast.LENGTH_SHORT
			).show()
		}
	}
}
