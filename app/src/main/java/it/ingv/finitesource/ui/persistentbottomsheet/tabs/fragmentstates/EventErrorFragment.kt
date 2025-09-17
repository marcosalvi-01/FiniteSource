package it.ingv.finitesource.ui.persistentbottomsheet.tabs.fragmentstates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.ingv.finitesource.R
import it.ingv.finitesource.databinding.FragmentEventErrorBinding

/**
 * Fragment to display when there is an error loading the earthquake event details data.
 *
 * It is displayed instead of the correct fragment for that tab.
 *
 * See [ProductFragment].
 */
class EventErrorFragment : Fragment() {
	private val binding: FragmentEventErrorBinding by lazy {
		FragmentEventErrorBinding.inflate(layoutInflater)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		// set the text
		binding.textViewEventError.text = resources.getString(R.string.event_error)
		// TODO add a button to try again and use different texts for different products
	}
}
