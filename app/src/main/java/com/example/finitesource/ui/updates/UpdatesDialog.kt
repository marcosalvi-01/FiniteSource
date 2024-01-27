package com.example.finitesource.ui.updates

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getString
import com.example.finitesource.R
import com.example.finitesource.data.local.EarthquakeUpdates
import com.example.finitesource.databinding.UpdatesDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// UpdatesDialog class definition
class UpdatesDialog(
	context: Context,
	updates: EarthquakeUpdates
) {
	// AlertDialog instance
	private val alertDialog: AlertDialog

	// Method to show the dialog
	fun show() {
		alertDialog.show()
	}

	// Method to dismiss the dialog
	fun dismiss() {
		alertDialog.dismiss()
	}

	init {
		// Inflate the layout for the dialog
		val updatesDialogBinding =
			UpdatesDialogBinding.inflate(LayoutInflater.from(context))

		// Count the number of new events and the total number of new products
		val newEvents = updates.newEarthquakes.size
		val newProducts = updates.newProducts.size
		val updatedProducts = updates.finiteSourceUpdated.size

		// Hide the TextViews for new events, new products, and updated products if there are no updates
		if (newEvents <= 0)
			updatesDialogBinding.newEvents.visibility = View.GONE
		if (newProducts <= 0)
			updatesDialogBinding.newProducts.visibility = View.GONE
		if (updatedProducts <= 0)
			updatesDialogBinding.updatedProducts.visibility = View.GONE

		// Set the text for the TextViews
		updatesDialogBinding.sinceLastTime.text = getString(context, R.string.since_last_time)
		updatesDialogBinding.newEvents.text =
			context.resources.getQuantityString(
				R.plurals.new_earthquakes,
				newEvents,
				newEvents
			)
		updatesDialogBinding.newProducts.text =
			context.resources.getQuantityString(
				R.plurals.new_products,
				newProducts,
				newProducts
			)
		updatesDialogBinding.updatedProducts.text =
			context.resources.getQuantityString(
				R.plurals.updated_finite_source,
				updatedProducts,
				updatedProducts
			)

		// Create a MaterialAlertDialogBuilder
		alertDialog = MaterialAlertDialogBuilder(context).apply {
			// Set the title of the dialog
			this.setCustomTitle(TextView(context).apply {
				this.text = getString(context, R.string.what_is_new)
				this.textSize = 24f
				this.typeface = Typeface.DEFAULT_BOLD
				// Add padding to the title
				val padding =
					resources.getDimensionPixelSize(R.dimen.dialog_title_padding)
				this.setPadding(padding, padding, padding, padding)
			})

			// Set the view of the dialog
			this.setView(updatesDialogBinding.root)

			// Set the positive button of the dialog
			this.setPositiveButton(getString(context, R.string.more_info)) { dialog, which ->
				// TODO: Handle the positive button click
			}

			// Set the negative button of the dialog
			this.setNegativeButton(getString(context, R.string.ignore)) { dialog, _ ->
				dialog.dismiss()
			}
		}.create()
	}
}
