package com.example.finitesource.ui.persistentbottomsheet.tabs.scenarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finitesource.collapse
import com.example.finitesource.data.local.earthquake.focalplane.Scenario
import com.example.finitesource.databinding.ExpandableListItemBinding
import com.example.finitesource.expand

/**
 * Adapter for handling the expandable items in the [ScenariosFragment].
 */
class ExpandableItemAdapter(private val scenarios: List<Scenario>) :
	RecyclerView.Adapter<ExpandableItemAdapter.ViewHolder>() {

	// This property will hold the binding for the current item view
	private lateinit var binding: ExpandableListItemBinding

	// This function inflates the layout for an item view and returns a ViewHolder
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		binding = ExpandableListItemBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)
		return ViewHolder(binding)
	}

	// This function returns the number of items in the list
	override fun getItemCount(): Int {
		return scenarios.size
	}

	// This function binds data to the views inside an item view
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val scenario = scenarios[position]
		// Bind scenario data to views using data binding
		with(holder.binding) {
			itemTitle.text = scenario.name
			displacementMap.imageUrl = scenario.displacementMapUrl
			displacementMap.text = scenario.displacementMapDescription
			predictedFringes.imageUrl = scenario.predictedFringesUrl
			predictedFringes.text = scenario.predictedFringesDescription

			// Set click listener to handle item expansion/collapse
			itemHeader.setOnClickListener {
				if (holder.binding.itemExpanded.visibility == View.GONE) {
					// Rotate the arrow and expand the item
					holder.binding.itemExpandButton.animate().rotation(180F).start()
					holder.binding.itemExpanded.expand()
				} else {
					// Collapse the item and rotate the arrow
					holder.binding.itemExpanded.collapse()
					holder.binding.itemExpandButton.animate().rotation(0F).start()
				}
			}
		}
	}

	/**
	 * ViewHolder class that holds references to the views inside an item view.
	 */
	class ViewHolder(val binding: ExpandableListItemBinding) :
		RecyclerView.ViewHolder(binding.root)
}
