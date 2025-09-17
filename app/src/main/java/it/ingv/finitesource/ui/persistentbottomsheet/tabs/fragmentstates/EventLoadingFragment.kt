package it.ingv.finitesource.ui.persistentbottomsheet.tabs.fragmentstates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import it.ingv.finitesource.databinding.FragmentEventLoadingBinding

/**
 * Fragment to display while the data of the event details is being downloaded. It displays an
 * indeterminate [ProgressBar].
 *
 * It is displayed instead of the correct fragment for that tab. When the event finishes loading,
 * the correct fragment or the error fragment is displayed.
 *
 * See [ProductFragment].
 */
class EventLoadingFragment : Fragment() {
	// lazy initialization of the binding
	private val binding: FragmentEventLoadingBinding by lazy {
		FragmentEventLoadingBinding.inflate(layoutInflater)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return binding.root
	}
}
