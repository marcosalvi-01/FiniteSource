package com.example.finitesource.ui.mapview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.WindowManager
import org.osmdroid.library.R
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.constants.GeoConstants
import org.osmdroid.util.constants.GeoConstants.UnitOfMeasure
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay
import java.util.Locale
import kotlin.math.pow

// i just copied the OSMDROID source code and modified it to suit my needs
open class CustomScaleBarOverlay private constructor(
	private var mMapView: MapView?,
	pContext: Context,
	pMapWidth: Int,
	pMapHeight: Int
) :
	Overlay(), GeoConstants {
	enum class UnitsOfMeasure {
		metric, imperial, nautical
	}

	// Defaults
	var xOffset = 10
	var yOffset = 10
	var minZoom = 0.0
	var unitsOfMeasure: UnitsOfMeasure = UnitsOfMeasure.metric
		set(unitsOfMeasure) {
			field = unitsOfMeasure
			lastZoomLevel = -1.0 // Force redraw of scalebar
		}
	var latitudeBar = true
	var longitudeBar = false
	var alignBottom = false
		set(alignBottom) {
			centred = false
			field = alignBottom
			lastZoomLevel = -1.0 // Force redraw of scalebar
		}
	var alignRight = false
		set(alignRight) {
			centred = false
			field = alignRight
			lastZoomLevel = -1.0 // Force redraw of scalebar
		}

	// Internal
	private var context: Context?
	protected val barPath = Path()
	protected val latitudeBarRect = Rect()
	protected val longitudeBarRect = Rect()
	private var lastZoomLevel = -1.0
	private var lastLatitude = 0.0
	var xdpi: Float
	var ydpi: Float
	var screenWidth: Int
	var screenHeight: Int
	private var barPaint: Paint?
	private var bgPaint: Paint?
	private var textPaint: Paint?
	private var centred = false
	private var adjustLength = false
	private var maxLength: Float

	/**
	 * @since 6.1.0
	 */
	private val mMapWidth: Int

	/**
	 * @since 6.1.0
	 */
	private val mMapHeight: Int

	// ===========================================================
	// Constructors
	// ===========================================================
	constructor(mapView: MapView) : this(mapView, mapView.context, 0, 0)

	/**
	 * @since 6.1.0
	 */
	constructor(pContext: Context, pMapWidth: Int, pMapHeight: Int) : this(
		null,
		pContext,
		pMapWidth,
		pMapHeight
	)

	/**
	 * @since 6.1.0
	 */
	init {
		context = pContext
		mMapWidth = pMapWidth
		mMapHeight = pMapHeight
		val dm = context!!.resources.displayMetrics
		barPaint = Paint()
		barPaint!!.color = Color.BLACK
		barPaint!!.isAntiAlias = true
		barPaint!!.style = Paint.Style.STROKE
		barPaint!!.alpha = 255
		barPaint!!.strokeWidth = 2 * dm.density
		bgPaint = null
		textPaint = Paint()
		textPaint!!.color = Color.BLACK
		textPaint!!.isAntiAlias = true
		textPaint!!.style = Paint.Style.FILL
		textPaint!!.alpha = 255
		textPaint!!.textSize = 10 * dm.density
		xdpi = dm.xdpi
		ydpi = dm.ydpi
		screenWidth = dm.widthPixels
		screenHeight = dm.heightPixels

		// DPI corrections for specific models
		var manufacturer: String? = null
		try {
			val field = Build::class.java.getField("MANUFACTURER")
			manufacturer = field[null] as String
		} catch (ignore: Exception) {
		}
		if ("motorola" == manufacturer && "DROIDX" == Build.MODEL) {

			// If the screen is rotated, flip the x and y dpi values
			val windowManager = context!!
				.getSystemService(Context.WINDOW_SERVICE) as WindowManager
			if (windowManager.defaultDisplay.orientation > 0) {
				xdpi = (screenWidth / 3.75).toFloat()
				ydpi = (screenHeight / 2.1).toFloat()
			} else {
				xdpi = (screenWidth / 2.1).toFloat()
				ydpi = (screenHeight / 3.75).toFloat()
			}
		} else if ("motorola" == manufacturer && "Droid" == Build.MODEL) {
			// http://www.mail-archive.com/android-developers@googlegroups.com/msg109497.html
			xdpi = 264f
			ydpi = 264f
		}

		// set default max length to 1 inch
		maxLength = 2.54f
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * Sets the scale bar screen offset for the bar. Note: if the bar is set to be drawn centered,
	 * this will be the middle of the bar, otherwise the top left corner.
	 *
	 * @param x x screen offset
	 * @param y z screen offset
	 */
	fun setScaleBarOffset(x: Int, y: Int) {
		xOffset = x
		yOffset = y
	}

	/**
	 * Sets the bar's line width. (the default is 2)
	 *
	 * @param width the new line width
	 */
	fun setLineWidth(width: Float) {
		barPaint!!.strokeWidth = width
	}

	/**
	 * Sets the text size. (the default is 12)
	 *
	 * @param size the new text size
	 */
	fun setTextSize(size: Float) {
		textPaint!!.textSize = size
	}


	/**
	 * Latitudinal / horizontal scale bar flag
	 *
	 * @param latitude
	 */
	fun drawLatitudeScale(latitude: Boolean) {
		latitudeBar = latitude
		lastZoomLevel = -1.0 // Force redraw of scalebar
	}

	/**
	 * Longitudinal / vertical scale bar flag
	 *
	 * @param longitude
	 */
	fun drawLongitudeScale(longitude: Boolean) {
		longitudeBar = longitude
		lastZoomLevel = -1.0 // Force redraw of scalebar
	}

	/**
	 * Flag to draw the bar centered around the set offset coordinates or to the right/bottom of the
	 * coordinates (default)
	 *
	 * @param centred set true to centre the bar around the given screen coordinates
	 */
	fun setCentred(centred: Boolean) {
		this.centred = centred
		alignBottom = !centred
		alignRight = !centred
		lastZoomLevel = -1.0 // Force redraw of scalebar
	}

	/**
	 * Return's the paint used to draw the bar
	 *
	 * @return the paint used to draw the bar
	 */
	fun getBarPaint(): Paint? {
		return barPaint
	}

	/**
	 * Sets the paint for drawing the bar
	 *
	 * @param pBarPaint bar drawing paint
	 */
	fun setBarPaint(pBarPaint: Paint?) {
		requireNotNull(pBarPaint) { "pBarPaint argument cannot be null" }
		barPaint = pBarPaint
		lastZoomLevel = -1.0 // Force redraw of scalebar
	}

	/**
	 * Returns the paint used to draw the text
	 *
	 * @return the paint used to draw the text
	 */
	fun getTextPaint(): Paint? {
		return textPaint
	}

	/**
	 * Sets the paint for drawing the text
	 *
	 * @param pTextPaint text drawing paint
	 */
	fun setTextPaint(pTextPaint: Paint?) {
		requireNotNull(pTextPaint) { "pTextPaint argument cannot be null" }
		textPaint = pTextPaint
		lastZoomLevel = -1.0 // Force redraw of scalebar
	}

	/**
	 * Sets the background paint. Set to null to disable drawing of background (default)
	 *
	 * @param pBgPaint the paint for colouring the bar background
	 */
	fun setBackgroundPaint(pBgPaint: Paint?) {
		bgPaint = pBgPaint
		lastZoomLevel = -1.0 // Force redraw of scalebar
	}

	/**
	 * If enabled, the bar will automatically adjust the length to reflect a round number (starting
	 * with 1, 2 or 5). If disabled, the bar will always be drawn in full length representing a
	 * fractional distance.
	 */
	fun setEnableAdjustLength(adjustLength: Boolean) {
		this.adjustLength = adjustLength
		lastZoomLevel = -1.0 // Force redraw of scalebar
	}

	/**
	 * Sets the maximum bar length. If adjustLength is disabled this will match exactly the length
	 * of the bar. If adjustLength is enabled, the bar will be shortened to reflect a round number
	 * in length.
	 *
	 * @param pMaxLengthInCm maximum length of the bar in the screen in cm. Default is 2.54 (=1 inch)
	 */
	fun setMaxLength(pMaxLengthInCm: Float) {
		maxLength = pMaxLengthInCm
		lastZoomLevel = -1.0 // Force redraw of scalebar
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================
	override fun draw(c: Canvas, projection: Projection) {
		val zoomLevel = projection.zoomLevel
		if (zoomLevel < minZoom) {
			return
		}
		val rect = projection.intrinsicScreenRect
		val _screenWidth = rect.width()
		val _screenHeight = rect.height()
		val screenSizeChanged = _screenHeight != screenHeight || _screenWidth != screenWidth
		screenHeight = _screenHeight
		screenWidth = _screenWidth
		val center = projection.fromPixels(screenWidth / 2, screenHeight / 2, null)
		if (zoomLevel != lastZoomLevel || center.latitude != lastLatitude || screenSizeChanged) {
			lastZoomLevel = zoomLevel
			lastLatitude = center.latitude
			rebuildBarPath(projection)
		}
		var offsetX = xOffset
		var offsetY = yOffset
		if (alignBottom) offsetY *= -1
		if (alignRight) offsetX *= -1
		if (centred && latitudeBar) offsetX += -latitudeBarRect.width() / 2
		if (centred && longitudeBar) offsetY += -longitudeBarRect.height() / 2
		projection.save(c, false, true)
		c.translate(offsetX.toFloat(), offsetY.toFloat())
		if (latitudeBar && bgPaint != null) c.drawRect(latitudeBarRect, bgPaint!!)
		if (longitudeBar && bgPaint != null) {
			// Don't draw on top of latitude background...
			val offsetTop = if (latitudeBar) latitudeBarRect.height() else 0
			c.drawRect(
				longitudeBarRect.left.toFloat(), (longitudeBarRect.top + offsetTop).toFloat(),
				longitudeBarRect.right.toFloat(), longitudeBarRect.bottom.toFloat(), bgPaint!!
			)
		}
		c.drawPath(barPath, barPaint!!)
		if (latitudeBar) {
			drawLatitudeText(c, projection)
		}
		if (longitudeBar) {
			drawLongitudeText(c, projection)
		}
		projection.restore(c, true)
	}

	// ===========================================================
	// Methods
	// ===========================================================
	fun disableScaleBar() {
		isEnabled = false
	}

	fun enableScaleBar() {
		isEnabled = true
	}

	private fun drawLatitudeText(canvas: Canvas, projection: Projection) {
		// calculate dots per centimeter
		val xdpcm = (xdpi / 2.54).toInt()

		// get length in pixel
		val xLen = (maxLength * xdpcm).toInt()

		// Two points, xLen apart, at scale bar screen location
//		IGeoPoint p1 = projection.fromPixels((screenWidth / 2) - (xLen / 2), yOffset, null);
//		IGeoPoint p2 = projection.fromPixels((screenWidth / 2) + (xLen / 2), yOffset, null);
		val p1 = projection.fromPixels(screenWidth / 2 - xLen / 2, screenHeight / 2, null)
		val p2 = projection.fromPixels(screenWidth / 2 + xLen / 2, screenHeight / 2, null)

		// get distance in meters between points
		val xMeters = (p1 as GeoPoint).distanceToAsDouble(p2)
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		val xMetersAdjusted = if (adjustLength) adjustScaleBarLength(xMeters) else xMeters
		// get adjusted length in pixels
		val xBarLengthPixels = (xLen * xMetersAdjusted / xMeters).toInt()

		// create text
		val xMsg = scaleBarLengthText(xMetersAdjusted)
		textPaint!!.getTextBounds(xMsg, 0, xMsg.length, sTextBoundsRect)
		val xTextSpacing = (sTextBoundsRect.height() / 5.0).toInt()
		var x = (xBarLengthPixels / 2 - sTextBoundsRect.width() / 2).toFloat()
		if (alignRight) x += (screenWidth - xBarLengthPixels).toFloat()
		val y: Float
		y = if (alignBottom) {
			(screenHeight - xTextSpacing * 2).toFloat()
		} else (sTextBoundsRect.height() + xTextSpacing).toFloat()
		canvas.drawText(xMsg, x, y, textPaint!!)
	}

	private fun drawLongitudeText(canvas: Canvas, projection: Projection) {
		// calculate dots per centimeter
		val ydpcm = (ydpi / 2.54).toInt()

		// get length in pixel
		val yLen = (maxLength * ydpcm).toInt()

		// Two points, yLen apart, at scale bar screen location
		val p1 = projection
			.fromPixels(screenWidth / 2, screenHeight / 2 - yLen / 2, null)
		val p2 = projection
			.fromPixels(screenWidth / 2, screenHeight / 2 + yLen / 2, null)

		// get distance in meters between points
		val yMeters = (p1 as GeoPoint).distanceToAsDouble(p2)
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		val yMetersAdjusted = if (adjustLength) adjustScaleBarLength(yMeters) else yMeters
		// get adjusted length in pixels
		val yBarLengthPixels = (yLen * yMetersAdjusted / yMeters).toInt()

		// create text
		val yMsg = scaleBarLengthText(yMetersAdjusted)
		textPaint!!.getTextBounds(yMsg, 0, yMsg.length, sTextBoundsRect)
		val yTextSpacing = (sTextBoundsRect.height() / 5.0).toInt()
		val x: Float
		x = if (alignRight) {
			(screenWidth - yTextSpacing * 2).toFloat()
		} else (sTextBoundsRect.height() + yTextSpacing).toFloat()
		var y = (yBarLengthPixels / 2 + sTextBoundsRect.width() / 2).toFloat()
		if (alignBottom) y += (screenHeight - yBarLengthPixels).toFloat()
		canvas.save()
		canvas.rotate(-90f, x, y)
		canvas.drawText(yMsg, x, y, textPaint!!)
		canvas.restore()
	}

	protected fun rebuildBarPath(projection: Projection) {   //** modified to protected
		// We want the scale bar to be as long as the closest round-number miles/kilometers
		// to 1-inch at the latitude at the current center of the screen.

		// calculate dots per centimeter
		val xdpcm = (xdpi / 2.54).toInt()
		val ydpcm = (ydpi / 2.54).toInt()

		// get length in pixel
		val xLen = (maxLength * xdpcm).toInt()
		val yLen = (maxLength * ydpcm).toInt()

		// Two points, xLen apart, at scale bar screen location
		var p1 = projection.fromPixels(screenWidth / 2 - xLen / 2, yOffset, null)
		var p2 = projection.fromPixels(screenWidth / 2 + xLen / 2, yOffset, null)

		// get distance in meters between points
		val xMeters = (p1 as GeoPoint).distanceToAsDouble(p2)
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		val xMetersAdjusted = if (adjustLength) adjustScaleBarLength(xMeters) else xMeters
		// get adjusted length in pixels
		val xBarLengthPixels = (xLen * xMetersAdjusted / xMeters).toInt()

		// Two points, yLen apart, at scale bar screen location
		p1 = projection.fromPixels(screenWidth / 2, screenHeight / 2 - yLen / 2, null)
		p2 = projection.fromPixels(screenWidth / 2, screenHeight / 2 + yLen / 2, null)

		// get distance in meters between points
		val yMeters = (p1 as GeoPoint).distanceToAsDouble(p2)
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		val yMetersAdjusted = if (adjustLength) adjustScaleBarLength(yMeters) else yMeters
		// get adjusted length in pixels
		val yBarLengthPixels = (yLen * yMetersAdjusted / yMeters).toInt()

		// create text
		val xMsg = scaleBarLengthText(xMetersAdjusted)
		val xTextRect = Rect()
		textPaint!!.getTextBounds(xMsg, 0, xMsg.length, xTextRect)
		var xTextSpacing = (xTextRect.height() / 5.0).toInt()

		// create text
		val yMsg = scaleBarLengthText(yMetersAdjusted)
		val yTextRect = Rect()
		textPaint!!.getTextBounds(yMsg, 0, yMsg.length, yTextRect)
		var yTextSpacing = (yTextRect.height() / 5.0).toInt()
		var xTextHeight = xTextRect.height()
		var yTextHeight = yTextRect.height()
		barPath.rewind()

		//** alignBottom ad-ons
		var barOriginX = 0
		var barOriginY = 0
		var barToX = xBarLengthPixels
		var barToY = yBarLengthPixels
		if (alignBottom) {
			xTextSpacing *= -1
			xTextHeight *= -1
			barOriginY = mapHeight
			barToY = barOriginY - yBarLengthPixels
		}
		if (alignRight) {
			yTextSpacing *= -1
			yTextHeight *= -1
			barOriginX = mapWidth
			barToX = barOriginX - xBarLengthPixels
		}
		if (latitudeBar) {
			// draw latitude bar
			barPath.moveTo(
				barToX.toFloat(),
				(barOriginY + xTextHeight + xTextSpacing * 2).toFloat()
			)
			barPath.lineTo(barToX.toFloat(), barOriginY.toFloat())
			barPath.lineTo(barOriginX.toFloat(), barOriginY.toFloat())
			if (!longitudeBar) {
				barPath.lineTo(
					barOriginX.toFloat(),
					(barOriginY + xTextHeight + xTextSpacing * 2).toFloat()
				)
			}
			latitudeBarRect[barOriginX, barOriginY, barToX] =
				barOriginY + xTextHeight + xTextSpacing * 2
		}
		if (longitudeBar) {
			// draw longitude bar
			if (!latitudeBar) {
				barPath.moveTo(
					(barOriginX + yTextHeight + yTextSpacing * 2).toFloat(),
					barOriginY.toFloat()
				)
				barPath.lineTo(barOriginX.toFloat(), barOriginY.toFloat())
			}
			barPath.lineTo(barOriginX.toFloat(), barToY.toFloat())
			barPath.lineTo(
				(barOriginX + yTextHeight + yTextSpacing * 2).toFloat(),
				barToY.toFloat()
			)
			longitudeBarRect[barOriginX, barOriginY, barOriginX + yTextHeight + yTextSpacing * 2] =
				barToY
		}
	}

	/**
	 * Returns a reduced length that starts with 1, 2 or 5 and trailing zeros. If set to nautical or
	 * imperial the input will be transformed before and after the reduction so that the result
	 * holds in that respective unit.
	 *
	 * @param length length to round
	 * @return reduced, rounded (in m, nm or mi depending on setting) result
	 */
	private fun adjustScaleBarLength(length: Double): Double {
		var length = length
		var pow: Long = 0
		var feet = false
		if (unitsOfMeasure == UnitsOfMeasure.imperial) {
			if (length >= GeoConstants.METERS_PER_STATUTE_MILE / 5)
				length /= GeoConstants.METERS_PER_STATUTE_MILE
			else {
				length *= GeoConstants.FEET_PER_METER
				feet = true
			}
		} else if (unitsOfMeasure == UnitsOfMeasure.nautical) {
			if (length >= GeoConstants.METERS_PER_NAUTICAL_MILE / 5)
				length /= GeoConstants.METERS_PER_NAUTICAL_MILE
			else {
				length *= GeoConstants.FEET_PER_METER
				feet = true
			}
		}
		while (length >= 10) {
			pow++
			length /= 10.0
		}
		while (length < 1 && length > 0) {
			pow--
			length *= 10.0
		}
		length = if (length < 2) {
			1.0
		} else if (length < 5) {
			2.0
		} else {
			5.0
		}
		if (feet)
			length /= GeoConstants.FEET_PER_METER
		else if (unitsOfMeasure == UnitsOfMeasure.imperial)
			length *= GeoConstants.METERS_PER_STATUTE_MILE
		else if (unitsOfMeasure == UnitsOfMeasure.nautical)
			length *= GeoConstants.METERS_PER_NAUTICAL_MILE
		length *= 10.0.pow(pow.toDouble())
		return length
	}

	protected fun scaleBarLengthText(meters: Double): String {
		return when (unitsOfMeasure) {
			UnitsOfMeasure.metric -> if (meters >= 1000 * 5) {
				getConvertedScaleString(meters, UnitOfMeasure.kilometer, "%.0f")
			} else if (meters >= 1000 / 5) {
				getConvertedScaleString(meters, UnitOfMeasure.kilometer, "%.1f")
			} else if (meters >= 20) {
				getConvertedScaleString(meters, UnitOfMeasure.meter, "%.0f")
			} else {
				getConvertedScaleString(meters, UnitOfMeasure.meter, "%.2f")
			}

			UnitsOfMeasure.imperial -> if (meters >= GeoConstants.METERS_PER_STATUTE_MILE * 5) {
				getConvertedScaleString(meters, UnitOfMeasure.statuteMile, "%.0f")
			} else if (meters >= GeoConstants.METERS_PER_STATUTE_MILE / 5) {
				getConvertedScaleString(meters, UnitOfMeasure.statuteMile, "%.1f")
			} else {
				getConvertedScaleString(meters, UnitOfMeasure.foot, "%.0f")
			}

			UnitsOfMeasure.nautical -> if (meters >= GeoConstants.METERS_PER_NAUTICAL_MILE * 5) {
				getConvertedScaleString(meters, UnitOfMeasure.nauticalMile, "%.0f")
			} else if (meters >= GeoConstants.METERS_PER_NAUTICAL_MILE / 5) {
				getConvertedScaleString(meters, UnitOfMeasure.nauticalMile, "%.1f")
			} else {
				getConvertedScaleString(meters, UnitOfMeasure.foot, "%.0f")
			}

		}
	}

	override fun onDetach(mapView: MapView) {
		context = null
		mMapView = null
		barPaint = null
		bgPaint = null
		textPaint = null
	}

	/**
	 * @since 6.0.0
	 */
	private fun getConvertedScaleString(
		pMeters: Double,
		pConversion: UnitOfMeasure,
		pFormat: String
	): String {
		return getScaleString(
			context, String.format(
				Locale.getDefault(), pFormat,
				pMeters / pConversion.conversionFactorToMeters
			),
			pConversion
		)
	}

	private val mapWidth: Int
		/**
		 * @since 6.1.0
		 */
		get() = if (mMapView != null) mMapView!!.width else mMapWidth
	private val mapHeight: Int
		/**
		 * @since 6.1.0
		 */
		get() = if (mMapView != null) mMapView!!.height else mMapHeight

	companion object {
		// ===========================================================
		// Fields
		// ===========================================================
		private val sTextBoundsRect = Rect()

		/**
		 * @since 6.1.1
		 */
		fun getScaleString(
			pContext: Context?,
			pValue: String?,
			pUnitOfMeasure: UnitOfMeasure
		): String {
			return pContext!!.getString(
				R.string.format_distance_value_unit,
				pValue, pContext.getString(pUnitOfMeasure.stringResId)
			)
		}
	}
}