package it.ingv.finitesource.ui.updates

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getString
import it.ingv.finitesource.R
import it.ingv.finitesource.data.local.EarthquakeUpdates
import it.ingv.finitesource.databinding.UpdatesDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// UpdatesDialog class definition
class UpdatesDialog(
	context: Context,
	val updates: EarthquakeUpdates
) {
	// AlertDialog instance
	private val alertDialog: AlertDialog

	fun show() {
		alertDialog.show()
	}

	fun dismiss() {
		alertDialog.dismiss()
	}

	init {
		// Inflate the layout for the dialog
		val updatesDialogBinding =
			UpdatesDialogBinding.inflate(LayoutInflater.from(context))

		// Count the number of new events and the total number of new products
		val newEvents = updates.newEarthquakes.size
		val newProducts = updates.newFiniteSource.size
		val updatedProducts = updates.finiteSourceUpdated.size

		// Hide the TextViews for new events, new products, and updated products if there are no updates
		if (newEvents <= 0)
			updatesDialogBinding.newEvents.visibility = View.GONE
		if (newProducts <= 0)
			updatesDialogBinding.newProducts.visibility = View.GONE
		if (updatedProducts <= 0)
			updatesDialogBinding.updatedProducts.visibility = View.GONE

		// Set the text for the TextViews
		// TODO remove the plurals
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
			this.setPositiveButton(getString(context, R.string.more_info)) { _, _ ->
				// start the activity to show the updates
				val intent = Intent(context, UpdatesActivity::class.java)
				// convert the updates to EarthquakeUpdatesData
				val updatesData = EarthquakeUpdatesData.from(updates)
				// put the updates in the intent
				intent.putExtra(UPDATES_INTENT_EXTRA_KEY, updatesData)
				// start the activity
				ActivityCompat.startActivity(
					context,
					intent,
					null
				)
			}

			// Set the negative button of the dialog
			this.setNegativeButton(getString(context, R.string.ignore)) { dialog, _ ->
				dialog.dismiss()
			}
		}.create()
	}
}

const val UPDATES_INTENT_EXTRA_KEY = "updates_intent_extra_key"
