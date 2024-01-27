package com.example.finitesource.ui.imagetextview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.finitesource.IMAGE_URL_KEY
import com.example.finitesource.R
import com.example.finitesource.TITLE_ID_KEY
import com.example.finitesource.databinding.ImageTextViewBinding
import com.example.finitesource.saveImage
import com.example.finitesource.shareImage

/**
 * This class is a custom view with an image view and a text view.
 * The image view shows an image given by the [imageUrl] and the text view shows a text given by the [text].
 * The image view is clickable and shows the image full screen when clicked.
 * The image view has a popup menu that can be shown by long clicking the image view.
 * The popup menu has two options: save the image and share the image.
 */
class ImageTextView(
	context: Context,
	attrs: AttributeSet
) : FrameLayout(context, attrs) {
	private val binding: ImageTextViewBinding by lazy {
		ImageTextViewBinding.inflate(LayoutInflater.from(context), this, true)
	}

	/**
	 * The image view that shows the image
	 */
	val imageView = binding.imageView

	/**
	 * The text view that shows the text
	 */
	val textView = binding.textView

	/**
	 * The url of the image to be shown in the [imageView]
	 */
	var imageUrl: String? = null
		set(value) {
			field = value
			setGlideImage(value)
		}

	/**
	 * The text to be shown in the [textView]
	 */
	var text: String? = null
		set(value) {
			field = value
			_setText(value)
		}

	/**
	 * The resource id of the title of the image to be shown in the [FullScreenImageActivity]
	 */
	var fullScreenImageTitleId: Int? = null

	init {
		// set the long onclick listener to show the popup menu
		binding.imageViewContainer.setOnLongClickListener {
			// show the popup menu
			showContextMenu()
			true
		}

		// show the image full screen when clicked
		binding.imageViewContainer.setOnClickListener {
			val intent = Intent(getContext(), FullScreenImageActivity::class.java)
			intent.putExtra(IMAGE_URL_KEY, imageUrl)
			intent.putExtra(TITLE_ID_KEY, fullScreenImageTitleId)
			// Pass the transition name to the next activity
			val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
				context as Activity,
				// @string/full_screen_image_transition_name
				// is the transition name of the ImageView
				binding.imageView, ViewCompat.getTransitionName(binding.imageView)!!
			)
			// Start the activity with the transition
			ActivityCompat.startActivity(context, intent, options.toBundle())
		}
	}

	/**
	 * Sets the image to the [imageView] using Glide
	 */
	private fun setGlideImage(imageUrl: String?) {
		hideRootIfBothEmpty(imageUrl, text)
		// hide the image view and the progress bar if the image source is null
		if (imageUrl == null) {
			binding.imageView.visibility = GONE
			binding.progressBar.visibility = GONE
		} else {
			// else show the image
			binding.imageView.visibility = VISIBLE
			// Glide is used to load the image into the ImageView
			Glide.with(this)
				.asBitmap()
				.load(imageUrl)
				.fitCenter()
				// this is because if the image is too big, the imageView can't hold it, so we scale it down
				// the problem with this is that if the imageview is bigger than the image, there will be borders
				.override(2000, Target.SIZE_ORIGINAL)    // TODO find a better way to do this
				.into(object : CustomTarget<Bitmap?>() {
					override fun onResourceReady(
						resource: Bitmap,
						transition: Transition<in Bitmap?>?
					) {
						// set the size of the image
						setImageSize(resource, binding.imageView)
						// set the image
						binding.imageView.setImageBitmap(resource)
						binding.imageView.visibility = VISIBLE
						binding.progressBar.visibility = GONE
					}

					override fun onLoadFailed(errorDrawable: Drawable?) {
						super.onLoadFailed(errorDrawable)
						// clear the image url
						this@ImageTextView.imageUrl = null
						// TODO show an error message
					}

					override fun onLoadCleared(placeholder: Drawable?) {
						// clear the image
						binding.imageView.setImageDrawable(null)
					}
				})
		}
	}

	override fun showContextMenu(): Boolean {
		PopupMenu(context, binding.imageView).apply {
			// inflate the menu
			inflate(R.menu.fullscreen_image_options_menu)
			// set the listener to the menu items
			setOnMenuItemClickListener { item ->
				when (item.itemId) {
					R.id.save_image -> {
						saveImage(context, imageUrl!!, true)
						true
					}

					R.id.share_image -> {
						shareImage(context, imageUrl!!)
						true
					}

					else -> false
				}
			}
			// show the popup menu
			show()
		}
		return true
	}

	private fun _setText(text: String?) {
		hideRootIfBothEmpty(imageUrl, text)
		if (text == null) {
			binding.textView.visibility = GONE
			binding.progressBar.visibility = GONE
		} else {
			binding.textView.visibility = VISIBLE
			binding.textView.text = text
		}
	}

	private fun setImageSize(resource: Bitmap, view: ImageView) {
		// TODO don't use the dimen padding and margin but use the actual distance from the border
		// get the screen width
		val screenWidth = resources.displayMetrics.widthPixels
		val maxHeight = resources.getDimension(R.dimen.images_max_height).toInt()
		// calculate the dimension to keep the aspect ratio and fill the parent
		val width = screenWidth -
				(resources.getDimension(R.dimen.images_margin_horizontal) * 2).toInt() -
				(resources.getDimension(R.dimen.bottom_sheet_padding) * 2).toInt()
		val height =
			(width.toFloat() / resource.width.toFloat() * resource.height.toFloat())
		// set the dimension
		if (height > maxHeight) {
			view.layoutParams.height = maxHeight
			view.layoutParams.width =
				(maxHeight.toFloat() / height * width.toFloat()).toInt()
		} else
			view.layoutParams.height = height.toInt()
	}

	/**
	 * Hides the root view if both the [imageUrl] and the [text] are null
	 */
	private fun hideRootIfBothEmpty(imageUrl: String?, text: String?) {
		if (imageUrl == null && text == null)
			visibility = GONE
	}
}
