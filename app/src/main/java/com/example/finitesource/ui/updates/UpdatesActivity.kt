package com.example.finitesource.ui.updates

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finitesource.databinding.ActivityUpdatesBinding
import com.example.finitesource.isDarkTheme
import com.example.finitesource.lightStatusBar
import androidx.core.view.isGone

class UpdatesActivity : AppCompatActivity() {
	private val binding by lazy {
		ActivityUpdatesBinding.inflate(layoutInflater)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)
		// set the toolbar
		setSupportActionBar(binding.toolbar)
		// set the listener for the back button
		binding.toolbar.setNavigationOnClickListener {
			// finish the activity when the back button is pressed
			finish()
		}

		// if is light theme, set the status bar to light
		if (!isDarkTheme(this))
			lightStatusBar(window, true)

		// set the listener for the expandable text views
		// Set click listener to handle item expansion/collapse
		setupExpandableList(
			binding.newEarthquakesItemHeader,
			binding.newEarthquakesList,
			binding.newEarthquakesItemExpandButton
		)
		setupExpandableList(
			binding.newFiniteSourceEarthquakesItemHeader,
			binding.newFiniteSourceEarthquakesList,
			binding.newFiniteSourceEarthquakesItemExpandButton
		)
		setupExpandableList(
			binding.updatedFiniteSourceItemHeader,
			binding.updatedFiniteSourceEarthquakesList,
			binding.updatedFiniteSourceItemExpandButton
		)


		// get the updates from the intent
		val updates: EarthquakeUpdatesData? = intent.getParcelableExtra(UPDATES_INTENT_EXTRA_KEY)
		if (updates != null && updates.hasUpdates()) {
			// hide the no updates text and show the scroll view
			binding.noUpdatesText.visibility = View.GONE
			binding.scrollView.visibility = View.VISIBLE
			// new earthquakes
			setupRecyclerView(
				binding.newEarthquakesList,
				updates.newEarthquakes,
				binding.newEarthquakesContainer
			)
			// new finite source earthquakes
			setupRecyclerView(
				binding.newFiniteSourceEarthquakesList,
				updates.newFiniteSource,
				binding.newFiniteSourceEarthquakesContainer
			)
			// updated finite source earthquakes
			setupRecyclerView(
				binding.updatedFiniteSourceEarthquakesList,
				updates.finiteSourceUpdated,
				binding.updatedFiniteSourceEarthquakesContainer
			)
		} else {
			// hide the scroll view and show the no updates text
			binding.scrollView.visibility = View.GONE
			binding.noUpdatesText.visibility = View.VISIBLE
		}
	}

	private fun setupExpandableList(header: View, list: View, expandButton: View) {
		header.setOnClickListener {
			if (list.isGone) {
				// Rotate the arrow and expand the item
				expandButton.animate().rotation(180F).start()
				list.visibility = View.VISIBLE
			} else {
				// Collapse the item and rotate the arrow
				list.visibility = View.GONE
				expandButton.animate().rotation(0F).start()
			}
		}
	}

	private fun setupRecyclerView(
		recyclerView: RecyclerView,
		data: List<EarthquakeData>,
		container: View
	) {
		// TODO maybe is better to use something else than a recycler view
		if (data.isNotEmpty()) {
			recyclerView.adapter = UpdatesItemAdapter(data)
			recyclerView.layoutManager = object : LinearLayoutManager(this) {
				// disable scrolling
				override fun canScrollVertically(): Boolean {
					return false
				}
			}
		} else
			container.visibility = View.GONE
	}
}