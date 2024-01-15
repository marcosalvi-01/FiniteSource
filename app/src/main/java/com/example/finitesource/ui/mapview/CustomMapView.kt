package com.example.finitesource.ui.mapview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageButton
import androidx.lifecycle.LifecycleOwner
import com.example.finitesource.R
import com.example.finitesource.cmToPx
import com.example.finitesource.data.local.earthquake.Earthquake
import com.example.finitesource.pxToCm
import com.example.finitesource.viewmodels.EarthquakesViewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay

/**
 * This class is a subclass of [MapView] that extends its functionality by adding custom overlays
 * and handling specific map interactions.
 */
class CustomMapView(context: Context, attributeSet: AttributeSet) : MapView(context, attributeSet) {
	private lateinit var scaleBarOverlay: CustomScaleBarOverlay
	private var selectedMarker: CustomMarker? = null
	var earthquakesViewModel: EarthquakesViewModel? = null
		set(value) {
			// listen to changes in the ui state
			value!!.uiState.observe(context as LifecycleOwner) {
				if (it.selectedEarthquake != null) {
					// zoom to the bounding box
					zoomToBoundingBox(it.selectedEarthquake.boundingBox, true)
				}
			}
			field = value
		}
	var compassButton: ImageButton? = null
		set(value) {
			// on click rotate the map and the compass to the north
			value?.setOnClickListener {
				// rotate the map to the north
				mapOrientation = 0f
				// Animate compass rotation to 0
				it.animate().rotation(0f)
			}
			// remove the old rotation gesture overlay
			overlays.removeIf {
				it is CustomRotationGestureOverlay
			}
			// add the new rotation gesture overlay
			value?.let {
				overlays.add(CustomRotationGestureOverlay(this, it))
			}
			field = value
		}

	// this is used because when onRestoreInstanceState is called, the map might not be drawn yet
	// so we save the state and restore it the next time the map is drawn which might be after
	// configuration changes
	private var savedState: Bundle? = null

	init {
		// initialize the map view
		setTileSource(TileSourceFactory.OpenTopo)
		setMultiTouchControls(true)
		zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
		// limit the scrollable area to the world
		setScrollableAreaLimitLatitude(
			getTileSystem().maxLatitude, getTileSystem().minLatitude, 0
		)
		isVerticalMapRepetitionEnabled = false

		// set up the scale bar
		scaleBarInit()

		// deselect the selected event when the map is clicked
		overlays.add(0, MapEventsOverlay(object : MapEventsReceiver {
			override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
//                mapViewModel.mapClicked()
				return false
			}

			override fun longPressHelper(p: GeoPoint): Boolean {
				return false
			}
		}))

		// update the size of the markers based on the zoom level
		addMapListener(object : MapListener {
			override fun onScroll(event: ScrollEvent): Boolean {
				return false
			}

			override fun onZoom(event: ZoomEvent): Boolean {
				// change the markers based on the zoom level
				if (event.zoomLevel > BIG_MARKERS_ZOOM_LEVEL)
					overlays.forEach {
						if (it is CustomMarker)
							it.setBigIcon()
					}
				else
					overlays.forEach {
						if (it is CustomMarker)
							it.setSmallIcon()
					}
				return false
			}
		})

		// calculate the bounding box of the screen
		val boundingBox = calculateScreenBoundingBox()

		// when the map is drawn for the first time
		addOnFirstLayoutListener { _, _, _, _, _ ->
			// zoom to the bounding box
			zoomToBoundingBox(boundingBox, false)
			// set the minimum zoom level the same as the current zoom level
			minZoomLevel = zoomLevelDouble
			// set the center to europe/africa
			controller.setCenter(EUROPE_CENTER)

			// observe the event channel
//            EventBus.observe(context as LifecycleOwner) {
//                when (it.type) {
//                    BOTTOM_SHEET_SLIDE -> {
//                        it.data as BottomSheetSlideEvent
//                        // move the scalebar up and down with the bottom sheet
//                        val offset =
//                            (it.data.slideOffset * (it.data.viewHeight - it.data.peekHeight - it.data.expandedOffset)
//                                    + it.data.peekHeight) + resources.getDimension(R.dimen.scale_bar_bottom_margin)
//
//                        scaleBarOverlay.setScaleBarOffset(scaleBarOverlay.xOffset, offset.toInt())
//                        invalidate()
//                    }
//
//                    EARTHQUAKE_EVENT_DESELECTED -> {
//                        if (selectedMarker != null && zoomLevelDouble > BIG_MARKERS_ZOOM_LEVEL)
//                        // zoom out a little
//                            zoomToBoundingBox(
//                                getBoundingBox().increaseByScale(ZOOM_OUT_FACTOR),
//                                true
//                            )
//                        selectedMarker = null
//                    }
//
//                    SLIP_ALPHA_SLIDER_SLIDE -> {
//                        // set the alpha of the slip polygons
//                        setSlipAlpha(it.data as Int)
//                    }
//
//                    COMPASS_BUTTON_CLICKED -> {
//                        // rotate the map to the north
//                        mapOrientation = 0f
//                    }
//                }
//            }
//
//            // observe the selected event
//            mapViewModel.selectedEarthquakeEventLiveData.observe(context as LifecycleOwner) { earthquakeEventResource ->
//                when (earthquakeEventResource) {
//                    is Resource.Selected -> {
//                        // when an event gets selected zoom to it
//                        zoomToBoundingBox(earthquakeEventResource.data.boundingBox, true)
//                        // if the selected event is different from the previous one
//                        selectedMarker?.takeIf { it.eventId != earthquakeEventResource.data.id }
//                            ?.let {
//                                overlays.add(it)
//                            }
//                    }
//
//                    is Resource.Loaded -> {
//                        // only hide the marker if the event has loaded
//                        val earthquakeEvent = earthquakeEventResource.data
//                        // if the event has finite source
//                        // remove the marker of the selected event
//                        overlays.forEach {
//                            if (it is CustomMarker && it.eventId == earthquakeEvent.id) {
//                                if (earthquakeEvent.availableProducts.contains(Product.FINITE_SOURCE))
//                                    overlays.remove(it)
//                                selectedMarker = it
//                            }
//                        }
//                    }
//
//                    is Resource.Error -> {
//                        // if there is an error loading the event show a toast
//                        Toast.makeText(
//                            context,
//                            earthquakeEventResource.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                    else -> { // is null (no selected event)
//                        // deselect the event
//                        // based on the zoom
//                        if (zoomLevelDouble > BIG_MARKERS_ZOOM_LEVEL)
//                        // make the marker big
//                            selectedMarker?.setBigIcon()
//                        else
//                        // make the marker small
//                            selectedMarker?.setSmallIcon()
//                        // show the last selected marker
//                        selectedMarker?.let { overlays.add(it) }
//                        // remove the old polygons
//                        overlays.removeIf { overlay: Overlay? ->
//                            overlay is CustomPolygon || overlay is Polyline
//                        }
//                    }
//                }
//                invalidate()
//            }
//
//            // observe the selected event's selected focal plane
//            mapViewModel.selectedFocalPlaneLiveData.observe(context as LifecycleOwner) { focalPlane ->
//                // remove the old polygons
//                overlays.removeIf { overlay: Overlay? ->
//                    overlay is CustomPolygon || overlay is Polyline
//                }
//                // hide the scale bar
//                overlays.remove(scaleBarOverlay)
//                focalPlane?.finiteSource?.let { finiteSource ->
//                    // draw the polygons on the map
//                    featuresToPoly(finiteSource.featureCollection, finiteSource.maxSlip)
//                        .forEach { overlays.add(it) }
//                    // show the scale bar after the polygons are added
//                    overlays.add(scaleBarOverlay)
//                }
//                invalidate()
//            }
		}
	}

	// set the alpha of the slip polygons
	private fun setSlipAlpha(alpha: Int) {
		overlays.filterIsInstance<CustomPolygon>().forEach { it.fillPaint.alpha = alpha }
		invalidate()
	}

	// computes the color of the polygon based on the slip
	private fun computeColor(slip: Double, maxSlip: Double): Int {
		val colorArray = context.resources.getIntArray(R.array.color_palette)
		return if (slip > 0) {
			val ratio = slip / maxSlip
			val index = (ratio * (colorArray.size - 1)).toInt()
			Color.rgb(
				Color.red(colorArray[index]),
				Color.green(colorArray[index]),
				Color.blue(colorArray[index])
			)
		} else Color.WHITE
	}

	private fun scaleBarInit() {
		scaleBarOverlay = CustomScaleBarOverlay(this).apply {
			// set the scale bar overlay
			alignBottom = true
			setTextSize(resources.getDimension(R.dimen.scale_bar_text_size))
			// get the screen width
			val displayMetrics = getDisplayMetrics()
			val scaleBarHorizontalMargin =
				resources.getDimension(R.dimen.scale_bar_horizontal_margin).toInt()
			val scaleBarScreenWidthCm: Float = pxToCm(
				(displayMetrics.widthPixels - scaleBarHorizontalMargin * 2).toFloat(),
				context
			)
			// calculate the length of the scale bar based on the screen
			setMaxLength(
				SCALE_BAR_MAX_WIDTH_CM.coerceAtMost(scaleBarScreenWidthCm)
			)
			xOffset = if (scaleBarScreenWidthCm > SCALE_BAR_MAX_WIDTH_CM) {
				val maxWidthPx: Int = cmToPx(SCALE_BAR_MAX_WIDTH_CM, context).toInt()
				(displayMetrics.widthPixels - maxWidthPx) / 2
			} else
				scaleBarHorizontalMargin
			setScaleBarOffset(
				xOffset,
				yOffset + context.resources.getDimension(R.dimen.bottom_sheet_peek_height).toInt()
			)
		}
	}

	fun setEarthquakes(events: List<Earthquake>) {
		// sort the events by date
		events.sortedByDescending { it.date }
		for (event in events) {
			val marker = CustomMarker(
				this,
				event.date,
				event.magnitude,
				GeoPoint(event.latitude, event.longitude),
				event.finiteSourceLastUpdate != null,
				event.id,
				event.boundingBox
			)
			marker.setOnMarkerClickListener { _, _ ->
				// when the marker is clicked, call the view model
				earthquakesViewModel?.selectEarthquake(event)
				true
			}
			// add the marker to the map
			overlays.add(marker)
		}
		// update the map
		invalidate()
	}

	private fun calculateScreenBoundingBox(): BoundingBox {
		// TODO on some devices the bounding box is not correct
		// calculate a bounding box that covers the screen
		// to set the min zoom level
		val displayMetrics = getDisplayMetrics()
		val screenHeight = displayMetrics.heightPixels
		val screenWidth = displayMetrics.widthPixels

		// projection to convert pixels to lat/lon
		val topLeft = projection.fromPixels(0, 0)
		val bottomRight = projection.fromPixels(screenWidth / 2, screenHeight / 2)
		// north, east, south, west
		return BoundingBox(
			topLeft.latitude,
			bottomRight.longitude,
			bottomRight.latitude,
			topLeft.longitude
		)
	}

	// convert the features to poly-something to draw them on the map
//	private fun featuresToPoly(
//		featureCollection: FeatureCollection,
//		maxSlip: Double
//	): MutableSet<Overlay> {
//		val polygons = mutableSetOf<Overlay>()
//		featureCollection.features.forEach { feature ->
//			// get the geometry of the feature
//			// as a Jackson Polygon
//			when (val poly = feature.geometry) {
//				// convert the polygon to a CustomPolygon
//				is Polygon -> polygons.add(
//					osmdroidPolygonFromJacksonPolygon(
//						poly,
//						feature,
//						maxSlip
//					)
//				)
//
//				// convert the MultiLineString to a Polyline
//				is MultiLineString -> polygons.addAll(
//					osmdroidPolylinesFromJacksonMultiLineString(
//						poly
//					)
//				)
//			}
//		}
//		return polygons
//	}

//	private fun osmdroidPolygonFromJacksonPolygon(
//		poly: Polygon,
//		feature: Feature,
//		maxSlip: Double
//	): CustomPolygon {
//		// Convert the Jackson Polygon to a CustomPolygon (extends Osmdroid Polygon)
//		val geoPoints = poly.coordinates[0].map { coordinate ->
//			GeoPoint(coordinate.latitude, coordinate.longitude)
//		}
//		return CustomPolygon(
//			feature.getProperty(SLIP_KEY),
//			feature.getProperty(RAKE_KEY)
//		).apply {
//			points = geoPoints
//			outlinePaint.strokeWidth = 2.0f
//			outlinePaint.color = Color.GRAY
//			fillPaint.color = computeColor(slip, maxSlip)
//		}
//	}
//
//	private fun osmdroidPolylinesFromJacksonMultiLineString(multiLineString: MultiLineString): List<Polyline> {
//		// convert the Jackson MultiLineString to a Polylines
//		return multiLineString.coordinates.map { coordinate ->
//			val geoPointsLine = coordinate.map { lngLatAlt ->
//				GeoPoint(lngLatAlt.latitude, lngLatAlt.longitude)
//			}
//			Polyline().apply {
//				setPoints(geoPointsLine)
//				outlinePaint.strokeWidth = 6f
//				outlinePaint.color = Color.RED
//			}
//		}
//	}

	private fun getDisplayMetrics(): DisplayMetrics {
		val displayMetrics = DisplayMetrics()
		val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		windowManager.defaultDisplay.getMetrics(displayMetrics)
		return displayMetrics
	}

	override fun onSaveInstanceState(): Parcelable {
		val bundle = Bundle()
		// save this instance state
		bundle.putDouble(MAP_ZOOM_LEVEL_KEY, zoomLevelDouble)
		bundle.putDouble(MAP_CENTER_LAT_KEY, mapCenter.latitude)
		bundle.putDouble(MAP_CENTER_LON_KEY, mapCenter.longitude)
		bundle.putFloat(MAP_ORIENTATION_KEY, mapOrientation)
		bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
		// if the old instance was not consumed
		savedState?.let {
			// override the old instance state values
			bundle.putAll(it)
		}
		return bundle
	}

	override fun onRestoreInstanceState(state: Parcelable?) {
		val superState = if (state is Bundle) {
			// set the old state
			savedState = state
			// when the map is drawn for the first time
			addOnFirstLayoutListener { _, _, _, _, _ ->
				// restore the zoom level, center and rotation
				controller.setZoom(state.getDouble(MAP_ZOOM_LEVEL_KEY))
				controller.setCenter(
					GeoPoint(
						state.getDouble(MAP_CENTER_LAT_KEY),
						state.getDouble(MAP_CENTER_LON_KEY)
					)
				)
				mapOrientation = state.getFloat(MAP_ORIENTATION_KEY)
				// post an event on the bus to notify the compass button
//				EventBus.postEvent(Event(MAP_ROTATION_CHANGED, mapOrientation))
				// consume the old state
				savedState = null
			}
			state.getParcelable(SUPER_STATE_KEY)
		} else state
		super.onRestoreInstanceState(superState)
	}

	companion object {
		private val EUROPE_CENTER = GeoPoint(23.002782755921405, 23.56686325779671)
		private const val MAP_ZOOM_LEVEL_KEY = "zoomLevel"
		private const val MAP_CENTER_LAT_KEY = "mapCenterLatitude"
		private const val MAP_CENTER_LON_KEY = "mapCenterLongitude"
		private const val MAP_ORIENTATION_KEY = "mapRotation"
		private const val SUPER_STATE_KEY = "superState"
		private const val SCALE_BAR_MAX_WIDTH_CM = 6f
		private const val BIG_MARKERS_ZOOM_LEVEL = 9.0
		private const val ZOOM_OUT_FACTOR = 3f
	}
}