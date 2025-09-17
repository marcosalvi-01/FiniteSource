package it.ingv.finitesource.ui.updates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import it.ingv.finitesource.R
import it.ingv.finitesource.databinding.GlobalListItemBinding
import it.ingv.finitesource.formatDateTime
import it.ingv.finitesource.formatDepth
import it.ingv.finitesource.formatMagnitude

class UpdatesItemAdapter(
	private val updates: List<EarthquakeData>
) : RecyclerView.Adapter<UpdatesItemAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = GlobalListItemBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)
		return ViewHolder(binding)
	}

	override fun getItemCount(): Int {
		return updates.size
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		// Retrieve the current item from the list
		val currentItem = updates.elementAt(position)

		// Bind the data to the views using Data Binding
		holder.bind(currentItem)
	}

	/**
	 * ViewHolder for binding global event item data to the RecyclerView.
	 */
	class ViewHolder(private val binding: GlobalListItemBinding) :
		RecyclerView.ViewHolder(binding.root) {

		/**
		 * Binds the data to the views using Data Binding.
		 */
		fun bind(item: EarthquakeData) {
			binding.eventName.text = item.name
			binding.eventDate.text = formatDateTime(item.date.time)
			binding.eventDepth.text = formatDepth(binding.root.context, item.depth)
			binding.eventMagnitude.text = formatMagnitude(binding.root.context, item.magnitude)
			binding.root.background = AppCompatResources.getDrawable(
				binding.root.context,
				R.drawable.updates_list_item_background
			)
		}
	}
}
