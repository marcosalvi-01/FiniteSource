package com.example.finitesource

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.finitesource.ui.imagetextview.ImageTextView
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

fun offsetDateTimeToCalendar(offsetDateTime: OffsetDateTime?): Calendar? {
	if (offsetDateTime == null)
		return null
	val date = Date.from(offsetDateTime.toInstant())
	val calendar = Calendar.getInstance()
	calendar.time = date
	return calendar
}

// Helper function to get the locale suffix for the text files
fun getLocaleSuffix(): String {
	return when (Locale.getDefault().language) {
		"it" -> "it"
		else -> "en"
	}
}

fun pxToCm(px: Float, context: Context): Float {
	val dm = context.resources.displayMetrics
	return px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm) / 10
}

/**
 * Convert a dp value to a px value using the display density
 */
fun cmToPx(cm: Float, context: Context): Float {
	val dm = context.resources.displayMetrics
	return cm * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm) * 10
}

/**
 * Used by the data binding to format a date into a TextView
 */
fun formatDateTime(date: Date): String {
	// Format the Date object to the desired format
	val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
	return "${dateFormat.format(date)} UTC"
}

/**
 * Used by the data binding to format a depth into a TextView
 */
fun formatDepth(context: Context, depth: Double) =
	// Format the Date object to the desired format
	context.getString(
		R.string.global_list_item_depth, String.format(
			Locale.US, "%.1f", depth
		)
	)


/**
 * Used by the data binding to format a magnitude into a TextView
 */
fun formatMagnitude(context: Context, magnitude: Double) =
	// Format the Date object to the desired format
	context.getString(
		R.string.global_list_item_magnitude, String.format(
			Locale.US, "%.1f", magnitude
		)
	)

/**
 * Expand the given view with an animation
 *
 * See [Android: Expand/collapse animation](https://stackoverflow.com/questions/4946295/android-expand-collapse-animation)
 */
fun View.expand() {
	// TODO fix the view flickering at the start of the animation
	visibility = View.VISIBLE

	val matchParentMeasureSpec =
		View.MeasureSpec.makeMeasureSpec((parent as View).width, View.MeasureSpec.EXACTLY)
	val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
	measure(matchParentMeasureSpec, wrapContentMeasureSpec)
	val targetHeight = measuredHeight

	// Create a custom animation for height expansion
	val animation = object : Animation() {
		override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
			layoutParams.height = if (interpolatedTime == 1f) {
				ViewGroup.LayoutParams.WRAP_CONTENT
			} else
				(targetHeight * interpolatedTime).toInt()
			requestLayout()
		}

		override fun willChangeBounds(): Boolean {
			return true
		}
	}

	// Calculate animation duration based on target height and density
	animation.duration = (targetHeight / context.resources.displayMetrics.density).toInt().toLong()
	startAnimation(animation)
}

/**
 * Collapse the given view with an animation.
 *
 * See [Android: Expand/collapse animation](https://stackoverflow.com/questions/4946295/android-expand-collapse-animation)
 */
fun View.collapse() {
	val initialHeight = measuredHeight

	// Create a custom animation for height collapse
	val animation = object : Animation() {
		override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
			if (interpolatedTime == 1f) {
				visibility = View.GONE
			} else {
				layoutParams.height = (initialHeight * (1 - interpolatedTime)).toInt()
				requestLayout()
			}
		}

		override fun willChangeBounds(): Boolean {
			return true
		}
	}

	// Calculate animation duration based on initial height and density
	animation.duration =
		(initialHeight / context.resources.displayMetrics.density / 2).toInt().toLong()
	startAnimation(animation)
}

/**
 * Sets the title for the [FullScreenImageActivity] for each [ImageTextView] given.
 */
fun setTitleId(titleId: Int, vararg imageTextViews: ImageTextView) {
	for (imageTextView in imageTextViews)
		imageTextView.fullScreenImageTitleId = titleId
}

/**
 * Using [Glide], it gets the image from the url and calls the function with the bitmap as It.
 */
fun getBitmap(context: Context, url: String, function: (Bitmap) -> (Unit)) {
	Glide.with(context)
		.asBitmap()
		.load(url)
		.into(object : CustomTarget<Bitmap?>() {
			override fun onResourceReady(
				resource: Bitmap,
				transition: Transition<in Bitmap?>?
			) {
				function(resource)
			}

			override fun onLoadCleared(placeholder: Drawable?) {}
		})
}

/**
 * Saves the image in the gallery and returns the Uri.
 */
fun getImageUri(context: Context, bitmap: Bitmap): Uri {
	val path = saveImage(context, bitmap)
	return Uri.parse(path)
}

/**
 * Saves the image in the gallery and returns the path. It also shows a toast.
 */
fun saveImage(context: Context, bitmap: Bitmap, showToast: Boolean = false): String {
	val bytes = ByteArrayOutputStream()
	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
	val path = MediaStore.Images.Media.insertImage(
		(context as Activity).contentResolver,
		bitmap,
		UUID.randomUUID().toString() + ".jpg",
		"image"
	)
	if (showToast)
		Toast.makeText(context, R.string.image_saved, Toast.LENGTH_SHORT).show()
	return path
}

/**
 * Starts an intent to share the image.
 * The image to be shared will be saved in the gallery.
 */
fun shareImage(context: Context, bitmap: Bitmap) {
	// get the uri of the image
	val uri = getImageUri(context, bitmap)
	// create the intent
	val intent = android.content.Intent().apply {
		action = android.content.Intent.ACTION_SEND
		putExtra(android.content.Intent.EXTRA_STREAM, uri)
		type = "image/*"
	}
	// start the activity
	context.startActivity(
		android.content.Intent.createChooser(
			intent,
			context.getString(R.string.share_image)
		)
	)
}

/**
 * Gets the image from the url, saves it in the gallery and shows a toast.
 */
fun saveImage(context: Context, url: String, showToast: Boolean = false) {
	getBitmap(context, url) {
		saveImage(context, it, showToast)
	}
}

/**
 * Gets the image from the url and starts an intent to share it.
 */
fun shareImage(context: Context, url: String) {
	getBitmap(context, url) {
		shareImage(context, it)
	}
}

/**
 * Returns true if the current theme is dark.
 */
fun isDarkTheme(context: Context): Boolean {
	return context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
}

/**
 * Sets the status bar color.
 *
 * If given true, it will set the status bar to the Light theme, otherwise it will set it to the Dark theme.
 */
fun lightStatusBar(window: Window, isLight: Boolean) {
	val windowInsetController = WindowCompat.getInsetsController(window, window.decorView)
	if (windowInsetController.isAppearanceLightStatusBars != isLight)
		windowInsetController.isAppearanceLightStatusBars = isLight
}

/**
 * Returns the height of the status bar.
 */
fun getStatusBarHeight(resources: Resources): Int {
	// TODO do this in a better way without using reflection
	var result = 0
	val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
	if (resourceId > 0) {
		result = resources.getDimensionPixelSize(resourceId)
	}
	return result
}