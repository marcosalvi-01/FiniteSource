package it.ingv.finitesource.ui.mapview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import it.ingv.finitesource.R
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomMarker(
	mapView: MapView,
	private val date: Calendar,
	private val magnitude: Double,
	location: GeoPoint,
	hasFiniteSource: Boolean,
	val eventId: String,
	val boundingBox: BoundingBox
) : Marker(mapView) {
	// TODO this class is a mess, i don't have the strength to clean it up
	private val markerDrawable: LayerDrawable
	private var size: Int
	private var isMagnitudeOn = false
	private var isDateOn = false
	private var textIconDrawable: Drawable? = null
	private var textDescriptionDrawable: Drawable? = null
	private val context = mapView.context

	fun setBigIcon() {
		setIconSize(context.resources.getDimension(R.dimen.marker_icon_size_big))
		showMagnitude()
		showDate()
	}

	fun setSmallIcon() {
		setIconSize(context.resources.getDimension(R.dimen.marker_icon_size_small))
		hideMagnitude()
		hideDate()
	}

	init {
		size = context.resources.getDimension(R.dimen.marker_icon_size_small).toInt()
		textLabelFontSize = context.resources.getDimension(R.dimen.icon_magnitude_text_size)
			.toInt()

		// load the drawable for the icon
		markerDrawable = (ContextCompat.getDrawable(
			context,
			R.drawable.event_circle_icon
		) as LayerDrawable?)!!.mutate() as LayerDrawable
		markerDrawable.setBounds(0, 0, size, size)
		icon = markerDrawable
		// set the position
		position = location
		// set the color based on the existence of the extended source
		if (hasFiniteSource) setIconBackgroundColor(
			ContextCompat.getColor(
				context,
				R.color.has_finite_source
			)
		) else setIconBackgroundColor(
			ContextCompat.getColor(
				context, R.color.no_finite_source
			)
		)
		// Set the anchor point to the center of the marker
		setAnchor(ANCHOR_CENTER, ANCHOR_CENTER)

		setSmallIcon()
	}

	// adds the text to the center of the icon
	private fun setIconText(text: String) {
		// calculate the new icon only once
		if (textIconDrawable != null) {
			icon = textIconDrawable
			return
		}

		// Create a new bitmap with the desired dimensions
		val bitmap = Bitmap.createBitmap(
			mIcon.intrinsicWidth,
			mIcon.intrinsicHeight,
			Bitmap.Config.ARGB_8888
		)
		val canvas = Canvas(bitmap)
		// Clear the canvas with a transparent color
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
		// Draw the icon on the canvas
		mIcon.setBounds(0, 0, canvas.width, canvas.height)
		mIcon.draw(canvas)
		val textSize = context.resources.getDimension(R.dimen.icon_magnitude_text_size)
		// Draw the text on top of the icon
		val paint = Paint()
		paint.textSize = textSize
		paint.color = mTextLabelForegroundColor
		paint.isAntiAlias = true
		paint.typeface = Typeface.DEFAULT_BOLD
		paint.textAlign = Paint.Align.CENTER
		val x = (canvas.width / 2).toFloat()
		val y = (canvas.height - paint.ascent() - paint.descent()) / 2
		canvas.drawText(text, x, y, paint)
		// Create a new BitmapDrawable with the modified bitmap
		val newDrawable = BitmapDrawable(mResources, bitmap)
		// Set the bounds of the new drawable to match the original icon
		newDrawable.bounds = mIcon.bounds
		// Set the new drawable as the marker's icon
		icon = newDrawable
		// remember the drawable
		textIconDrawable = newDrawable
	}

	private fun showDate() {
		if (!isDateOn) {
			isDateOn = true
			// Display a date in day, month, year format
			// get locale date format short
			val formatter: DateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
			val date = formatter.format(date.time)
			setTextDescription(date)
		}
	}

	private fun hideDate() {
		if (isDateOn) {
			isDateOn = false
			icon = markerDrawable
		}
	}

	// adds a text over the icon
	private fun setTextDescription(text: String) {
		// Check if the text is the same as the previous one
		if (textDescriptionDrawable != null) {
			icon = textDescriptionDrawable
			return
		}
		val margin =
			context.resources.getDimension(R.dimen.icon_description_text_margin).toInt()
		val size =
			context.resources.getDimension(R.dimen.icon_description_text_size).toInt()

		// Create a paint object for measuring text dimensions
		val textPaint = Paint()
		textPaint.textSize = size.toFloat()
		textPaint.color = mTextLabelForegroundColor
		textPaint.isAntiAlias = true
		textPaint.typeface = Typeface.DEFAULT_BOLD

		// Calculate the width and height of the text
		val textWidth = textPaint.measureText(text).toInt()
		val textHeight = (textPaint.descent() - textPaint.ascent()).toInt()

		// Get the original icon as a bitmap
		val originalIconBitmap = (icon as BitmapDrawable).bitmap

		// Calculate the width and height of the original icon
		val iconWidth = originalIconBitmap.width
		val iconHeight = originalIconBitmap.height

		// Calculate the dimensions for the combined bitmap
		val combinedWidth = textWidth.coerceAtLeast(iconWidth)
		val combinedHeight = textHeight + iconHeight + margin

		// Create a bitmap with the combined dimensions
		val combinedBitmap =
			Bitmap.createBitmap(combinedWidth, combinedHeight, Bitmap.Config.ARGB_8888)

		// Create a canvas using the combined bitmap
		val canvas = Canvas(combinedBitmap)

		// Draw the text on the canvas
		canvas.drawText(
			text,
			((combinedWidth - textWidth) / 2).toFloat(),
			textHeight.toFloat(),
			textPaint
		)

		// Draw the original icon on the canvas
		canvas.drawBitmap(
			originalIconBitmap,
			((combinedWidth - iconWidth) / 2).toFloat(),
			(textHeight + margin).toFloat(),
			null
		)

		// Create a BitmapDrawable with the combined bitmap
		val combinedDrawable = BitmapDrawable(mResources, combinedBitmap)

		// Set the bounds of the combined drawable to match the original icon
		combinedDrawable.bounds = icon.bounds

		// Set the combined drawable as the marker's icon
		icon = combinedDrawable

		// Remember the drawable and text
		textDescriptionDrawable = combinedDrawable

		// Set the anchor point to the center of the marker
		setAnchor(ANCHOR_CENTER, ANCHOR_CENTER)
	}

	private fun setIconBackgroundColor(color: Int) {
		val background = markerDrawable.findDrawableByLayerId(R.id.background)
		background?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
			color,
			BlendModeCompat.SRC_ATOP
		)
	}

	private fun setIconSize(size: Float) {
		markerDrawable.setLayerSize(0, size.toInt(), size.toInt())
		this.size = size.toInt()
	}

	private fun showMagnitude() {
		if (!isMagnitudeOn) {
			isMagnitudeOn = true
			setIconText(magnitude.toString() + "")
		}
	}

	private fun hideMagnitude() {
		if (isMagnitudeOn) {
			isMagnitudeOn = false
			icon = markerDrawable
		}
	}
}
