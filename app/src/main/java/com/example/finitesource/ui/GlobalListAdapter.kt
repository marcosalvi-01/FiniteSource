package com.example.finitesource.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.finitesource.data.local.earthquake.Earthquake
import com.example.finitesource.databinding.GlobalListItemBinding
import com.example.finitesource.formatDateTime
import com.example.finitesource.formatDepth
import com.example.finitesource.formatMagnitude
import com.example.finitesource.viewmodels.EarthquakesViewModel
import java.util.Locale

/**
 * Adapter for displaying the list of global event items in a [RecyclerView].
 */
class GlobalListAdapter(
	private val viewModel: EarthquakesViewModel,
	lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<GlobalListAdapter.ViewHolder>(), Filterable {
	// TODO find a way to not use notifyDataSetChanged() when filtering, but instead use notifyItemInserted() and notifyItemRemoved() for better performance

	private var shownEventList = mutableListOf<Earthquake>()

	init {
		// Observe globalListItemLiveData for changes in global events data
		viewModel.earthquakes.observe(lifecycleOwner) { newData ->
			newData?.let {
				// Update the shownEventList with new data and refresh the view
				shownEventList = it.toMutableList()
				notifyDataSetChanged()
			}
		}
	}

	fun filterBySearchQuery(query: String) {
		filter.filter(query)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = GlobalListItemBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		// Retrieve the current item from the list
		val currentItem = shownEventList[position]

		// Bind the data to the views using Data Binding
		holder.bind(currentItem)

		// Set an onClickListener for the item
		holder.itemView.setOnClickListener {
			// Call the onItemClick method in the viewModel
			viewModel.selectEarthquake(currentItem)
		}
	}

	override fun getItemCount(): Int {
		return shownEventList.size
	}

	override fun getFilter(): Filter {
		return object : Filter() {
			override fun performFiltering(constraint: CharSequence?): FilterResults {
				val filteredResults = mutableListOf<Earthquake>()
				if (constraint.isNullOrBlank()) {
					filteredResults.addAll(
						viewModel.earthquakes.value ?: emptyList()
					)
				} else {
					val query = constraint.toString().lowercase(Locale.getDefault())
					for (item in viewModel.earthquakes.value ?: emptyList())
						if (item.toString().lowercase(Locale.getDefault()).contains(query))
							filteredResults.add(item)
				}
				return FilterResults().apply {
					values = filteredResults
				}
			}

			override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
				// if the results are of the correct type, update the shownEventList
				(results?.values as? List<*>)?.let {
					shownEventList.clear()
					shownEventList.addAll(it.filterIsInstance<Earthquake>())
					notifyDataSetChanged()
				}
			}
		}
	}

	/**
	 * ViewHolder for binding global event item data to the RecyclerView.
	 */
	class ViewHolder(private val binding: GlobalListItemBinding) :
		RecyclerView.ViewHolder(binding.root) {

		/**
		 * Binds the data to the views using Data Binding.
		 */
		fun bind(item: Earthquake) {
			binding.eventName.text = item.name
			binding.eventDate.text = formatDateTime(item.date.time)
			binding.eventDepth.text = formatDepth(binding.root.context, item.depth)
			binding.eventMagnitude.text = formatMagnitude(binding.root.context, item.magnitude)
		}
	}
}
