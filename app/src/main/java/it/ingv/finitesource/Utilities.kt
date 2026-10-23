package it.ingv.finitesource

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import it.ingv.finitesource.R
import it.ingv.finitesource.ui.imagetextview.ImageTextView
import java.io.IOException
import java.text.DateFormat
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.WeakHashMap

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
	layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

	val matchParentMeasureSpec =
		View.MeasureSpec.makeMeasureSpec((parent as View).width, View.MeasureSpec.EXACTLY)
	val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
	measure(matchParentMeasureSpec, wrapContentMeasureSpec)
	val targetHeight = measuredHeight

	// Establish the collapsed height before the first visible layout pass.
	layoutParams.height = 1
	visibility = View.VISIBLE

	// Create a custom animation for height expansion
	val animation = object : Animation() {
		override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
			layoutParams.height = if (interpolatedTime == 1f) {
				ViewGroup.LayoutParams.WRAP_CONTENT
			} else
				// Zero means MATCH_CONSTRAINT for children of ConstraintLayout.
				(targetHeight * interpolatedTime).toInt().coerceAtLeast(1)
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
fun getImageUri(context: Context, bitmap: Bitmap): Uri? {
	return saveImage(context, bitmap)?.let(Uri::parse)
}

/**
 * Saves the image in the gallery and returns the path. It also shows a toast.
 */
fun saveImage(context: Context, bitmap: Bitmap, showToast: Boolean = false): String? {
	val resolver = context.contentResolver
	var uri: Uri? = null
	return try {
		val name = UUID.randomUUID().toString() + ".jpg"
		val values = ContentValues().apply {
			put(MediaStore.Images.Media.DISPLAY_NAME, name)
			put(MediaStore.Images.Media.TITLE, name)
			put(MediaStore.Images.Media.DESCRIPTION, "image")
			put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
				put(MediaStore.Images.Media.IS_PENDING, 1)
		}
		val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
			?: throw IOException("Could not create gallery image")
		uri = imageUri
		val stream = resolver.openOutputStream(imageUri)
			?: throw IOException("Could not open gallery image")
		stream.use {
			// Match the JPEG quality used by the former MediaStore.insertImage implementation.
			if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 50, it))
				throw IOException("Could not encode gallery image")
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			val published = resolver.update(imageUri, ContentValues().apply {
				put(MediaStore.Images.Media.IS_PENDING, 0)
			}, null, null)
			if (published == 0) throw IOException("Could not publish gallery image")
		}
		if (showToast)
			Toast.makeText(context, R.string.image_saved, Toast.LENGTH_SHORT).show()
		imageUri.toString()
	} catch (e: Exception) {
		Log.e("FiniteSource", "Could not save image", e)
		uri?.let {
			runCatching { resolver.delete(it, null, null) }
				.onFailure { error -> Log.e("FiniteSource", "Could not remove incomplete image", error) }
		}
		Toast.makeText(context, R.string.image_save_failed, Toast.LENGTH_SHORT).show()
		null
	}
}

/**
 * Starts an intent to share the image.
 * The image to be shared will be saved in the gallery.
 */
fun shareImage(context: Context, bitmap: Bitmap) {
	// get the uri of the image
	val uri = getImageUri(context, bitmap) ?: return
	// create the intent
	val intent = android.content.Intent().apply {
		action = android.content.Intent.ACTION_SEND
		putExtra(android.content.Intent.EXTRA_STREAM, uri)
		type = "image/*"
		addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
		clipData = android.content.ClipData.newRawUri("image", uri)
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
	withGalleryPermission(context) {
		getBitmap(context, url) {
			saveImage(context, it, showToast)
		}
	}
}

/**
 * Gets the image from the url and starts an intent to share it.
 */
fun shareImage(context: Context, url: String) {
	withGalleryPermission(context) {
		getBitmap(context, url) {
			shareImage(context, it)
		}
	}
}

private val pendingGalleryPermissions = Collections.newSetFromMap(WeakHashMap<ComponentActivity, Boolean>())

internal fun withGalleryPermission(context: Context, action: () -> Unit) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(
			context, Manifest.permission.WRITE_EXTERNAL_STORAGE
		) == PackageManager.PERMISSION_GRANTED) {
		action()
		return
	}
	val activity = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
		.filterIsInstance<ComponentActivity>().firstOrNull()
	if (activity == null || activity.isDestroyed) {
		Toast.makeText(context, R.string.image_save_failed, Toast.LENGTH_SHORT).show()
		return
	}
	if (!pendingGalleryPermissions.add(activity)) return
	val key = "gallery_permission_${UUID.randomUUID()}"
	lateinit var launcher: ActivityResultLauncher<String>
	val observer = LifecycleEventObserver { _, event ->
		if (event == Lifecycle.Event.ON_DESTROY) {
			pendingGalleryPermissions.remove(activity)
			launcher.unregister()
		}
	}
	activity.lifecycle.addObserver(observer)
	launcher = activity.activityResultRegistry.register(key, ActivityResultContracts.RequestPermission()) { granted ->
		pendingGalleryPermissions.remove(activity)
		launcher.unregister()
		activity.lifecycle.removeObserver(observer)
		if (!activity.isDestroyed) {
			if (granted) action()
			else Toast.makeText(context, R.string.image_save_failed, Toast.LENGTH_SHORT).show()
		}
	}
	try {
		launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	} catch (e: RuntimeException) {
		pendingGalleryPermissions.remove(activity)
		launcher.unregister()
		activity.lifecycle.removeObserver(observer)
		Log.e("FiniteSource", "Could not request gallery permission", e)
		Toast.makeText(context, R.string.image_save_failed, Toast.LENGTH_SHORT).show()
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

/**
 * Checks if the application is launched for the first time.
 *
 * This function checks a flag in the shared preferences to determine if the app is launched for the first time.
 * If it is the first launch, it updates the flag in the shared preferences to indicate that the app has been launched before.
 *
 * @param context The context used to access the shared preferences.
 * @return A boolean indicating whether the app is launched for the first time. Returns true if it is the first launch, false otherwise.
 */
fun isFirstLaunch(context: Context): Boolean {
	// Get the shared preferences
	val sharedPreferences =
		context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
	// Get the flag indicating whether it is the first launch
	val isFirstLaunch = sharedPreferences.getBoolean("IS_FIRST_LAUNCH", true)

	if (isFirstLaunch) {
		// If it's the first launch, update the flag in shared preferences
		val editor = sharedPreferences.edit()
		editor.putBoolean("IS_FIRST_LAUNCH", false)
		editor.apply()
	}

	// Return whether it is the first launch
	return isFirstLaunch
}
