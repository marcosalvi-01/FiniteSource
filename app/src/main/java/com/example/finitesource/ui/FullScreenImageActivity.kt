package com.example.finitesource.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.finitesource.IMAGE_URL_KEY
import com.example.finitesource.R
import com.example.finitesource.TITLE_ID_KEY
import com.example.finitesource.databinding.ActivityFullscreenImageBinding
import com.example.finitesource.isDarkTheme
import com.example.finitesource.lightStatusBar
import com.example.finitesource.saveImage
import com.example.finitesource.shareImage

/**
 * This activity displays a full-screen image along with options to share and save the image.
 * The image is loaded using [Glide] and shown in a zoomable [SubsamplingScaleImageView].
 */
class FullScreenImageActivity : AppCompatActivity() {
	// Initialize the view binding lazily
	private val binding: ActivityFullscreenImageBinding by lazy {
		ActivityFullscreenImageBinding.inflate(layoutInflater)
	}

	// Extract the image URL and title ID from the intent's extras
	private val imageUrl by lazy {
		intent.getStringExtra(IMAGE_URL_KEY)!!
	}
	private val titleId by lazy {
		intent.getIntExtra(TITLE_ID_KEY, R.string.app_name)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		// Set the action bar to display the title
		setSupportActionBar(binding.toolbar)
		supportActionBar?.title = getString(titleId)
		supportActionBar?.show()

		// Set the status bar color based on the theme
		lightStatusBar(window, !isDarkTheme(this))

		// Load the image using Glide and display it in the ImageView
		Glide.with(this)
			.asBitmap()
			.load(imageUrl)
			.fitCenter()
			.into(object : CustomTarget<Bitmap?>() {
				override fun onResourceReady(
					resource: Bitmap,
					transition: Transition<in Bitmap?>?
				) {
					binding.fullScreenImageView.setImage(ImageSource.bitmap(resource))
				}

				override fun onLoadCleared(placeholder: Drawable?) {}
			})
	}

	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// Inflate the options menu
		menuInflater.inflate(R.menu.fullscreen_image_options_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.share_image -> {
				// Share the image
				shareImage(this, imageUrl)
				true
			}

			R.id.save_image -> {
				// Save the image
				saveImage(this, imageUrl, true)
				true
			}

			else -> super.onOptionsItemSelected(item)
		}
	}
}
