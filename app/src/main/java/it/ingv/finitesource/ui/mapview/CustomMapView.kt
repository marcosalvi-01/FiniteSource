package it.ingv.finitesource.ui.mapview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import it.ingv.finitesource.R
import it.ingv.finitesource.cmToPx
import it.ingv.finitesource.data.local.CatalogConfig
import it.ingv.finitesource.data.local.earthquake.Earthquake
import it.ingv.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson
import it.ingv.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJsonGeometryType
import it.ingv.finitesource.pxToCm
import it.ingv.finitesource.states.UiState
import it.ingv.finitesource.viewmodels.EarthquakesViewModel
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
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline

/**
 * This class is a subclass of [MapView] that extends its functionality by adding custom overlays
 * and handling specific map interactions.
 */
class CustomMapView(context: Context, attributeSet: AttributeSet) : MapView(context, attributeSet) {
    // In this class is a mess, the states are handled in a very bad way and it should be refactored (but it works)
    private lateinit var scaleBarOverlay: CustomScaleBarOverlay
    private var selectedMarker: CustomMarker? = null
    var earthquakesViewModel: EarthquakesViewModel? = null
        set(value) {
            // listen to changes in the ui state
            observeState(value!!.uiState)
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
                // This is done to prevent selecting an event and immediately deselecting it
                // before the event is loaded causing weird behavior
                // if there is no event that is loading or there is one but it has an error
                if (earthquakesViewModel?.uiState?.value?.loadingState?.loading == false
                    || earthquakesViewModel?.uiState?.value?.loadingState?.errorWhileLoading == true
                )
                // deselect the event
                    earthquakesViewModel?.deselectEarthquake()
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
                overlays.filterIsInstance<CustomMarker>().forEach { updateMarkerBasedOnZoom(it) }
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

    private fun updateMarkerBasedOnZoom(marker: CustomMarker) {
        // change the markers based on the zoom level
        if (zoomLevelDouble > BIG_MARKERS_ZOOM_LEVEL)
            marker.setBigIcon()
        else
            marker.setSmallIcon()
    }

    private fun observeState(uiStateLiveData: LiveData<UiState>) {
        uiStateLiveData.observe(context as LifecycleOwner) { uiState ->
            // if there is a selected event
            if (uiState.selectedEarthquake != null) {
                // if it is loading
                if (uiState.loadingState.loading) {
                    // update the selected marker
                    selectedMarker = overlays.firstOrNull {
                        it is CustomMarker && it.eventId == uiState.selectedEarthquake.id
                    } as CustomMarker?
                    // zoom to the bounding box
                    zoomToBoundingBox(uiState.selectedEarthquake.boundingBox, true)
                } else {    // if the event has loaded
                    // if there was an error while loading the event
                    if (uiState.loadingState.errorWhileLoading) {
                        // show a message
                        Toast.makeText(
                            context,
                            R.string.event_error,
                            Toast.LENGTH_SHORT
                        ).show() // TODO use a snackbar
                        // deselect the event
                        earthquakesViewModel?.deselectEarthquake()
                    } else {
                        // if the event has finite source
                        // remove the marker of the selected event
                        if (uiState.selectedEarthquake.hasFiniteSource()) {
                            overlays.removeIf {
                                it is CustomMarker && it.eventId == uiState.selectedEarthquake.id
                            }
                            // remove the old polygons (this is used when the user selects a new focal plane)
                            overlays.removeIf { overlay: Overlay? ->
                                overlay is CustomPolygon || overlay is Polyline
                            }
                            // show the finite source
                            overlays.addAll(
                                geoJsonToOsmdroidOverlays(
                                    uiState.selectedEarthquake.details!!.getFocalPlane(uiState.selectedFocalPlane)?.finiteSource!!.sourceJson
                                )
                            )
                            // show the scalebar
                            showScaleBar()
                        }
                    }
                }
            }
            // if there was a selected event deselect it
            else if (selectedMarker != null) {
                this.controller.animateTo(selectedMarker!!.position, 1.0, ZOOM_OUT_SPEED)

                // based on the zoom
                if (zoomLevelDouble > BIG_MARKERS_ZOOM_LEVEL) {
                    // make the marker big
                    selectedMarker!!.setBigIcon()
                    // zoom out a little
                } else {
                    // make the marker small
                    selectedMarker!!.setSmallIcon()
                }
                // show the last selected marker only if it is not already shown
                if (!overlays.contains(selectedMarker))
                    overlays.add(selectedMarker)
                // remove the old polygons
                overlays.removeIf { overlay: Overlay? ->
                    overlay is CustomPolygon || overlay is Polyline
                }
                // hide the scale bar
                hideScaleBar()
            }

            invalidate()
        }
    }

    // set the alpha of the slip polygons
    fun setSlipAlpha(alpha: Int) {
        overlays.filterIsInstance<CustomPolygon>().forEach { it.fillPaint.alpha = alpha }
        invalidate()
    }

    // computes the color of the polygon based on the slip
    private fun computeColor(slip: Double, maxSlip: Double): Int {
        val colorArray = CatalogConfig.slipColorPalette
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

    fun setScaleBarOffset(xOffset: Int, yOffset: Int) {
        scaleBarOverlay.setScaleBarOffset(xOffset, yOffset)
        invalidate()
    }

    fun setScaleBarYOffset(yOffset: Int) {
        setScaleBarOffset(scaleBarOverlay.xOffset, yOffset)
    }

    fun showScaleBar() {
        if (!overlays.contains(scaleBarOverlay)) {
            overlays.add(scaleBarOverlay)
            invalidate()
        }
    }

    fun hideScaleBar() {
        overlays.remove(scaleBarOverlay)
        invalidate()
    }

    fun setEarthquakes(events: List<Earthquake>) {
        val markers = mutableSetOf<CustomMarker>()
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
                try {
                    // select the new one
                    earthquakesViewModel?.selectEarthquake(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                        .show() // TODO use snackbar
                }
                true
            }
            // TODO find a better way to do this
            // add the marker to the markers set if it is not the selected one
            if (earthquakesViewModel?.uiState?.value?.selectedEarthquake?.id != event.id
                || earthquakesViewModel?.uiState?.value?.selectedEarthquake?.hasFiniteSource() == false
            )
                markers.add(marker)
            else
            // I have to do this because when the mapview is destroyed, the selected marker is not saved anywhere
            // so i have to save it here. This is a hack and it should be fixed
                selectedMarker = marker
        }
        // remove the old markers
        overlays.removeIf { it is CustomMarker }
        // update the markers based on the zoom level
        markers.forEach { updateMarkerBasedOnZoom(it) }
        // add the new markers
        overlays.addAll(markers)
        // update the mapview
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

    private fun geoJsonToOsmdroidOverlays(geoJson: CustomGeoJson): MutableSet<Overlay> {
        val finiteSource = mutableSetOf<Overlay>()
        geoJson.features.forEach { feature ->
            when (val geometryType = feature.geometry.type) {
                CustomGeoJsonGeometryType.POLYGON.value -> {
                    finiteSource.add(CustomPolygon(
                        feature.properties.slipM ?: 0.0,
                        feature.properties.rakeD ?: 0.0
                    ).apply {
                        points = feature.geometry.coordinates[0].map { coordinate ->
                            GeoPoint(coordinate[1], coordinate[0])
                        }
                        outlinePaint.strokeWidth = 2.0f
                        outlinePaint.color = Color.GRAY
                        fillPaint.color = computeColor(slip, geoJson.maxSlip)
                    })
                }

                CustomGeoJsonGeometryType.MULTI_LINE_STRING.value -> {
                    finiteSource.addAll(feature.geometry.coordinates.map { coordinate ->
                        val geoPointsLine = coordinate.map { lngLatAlt ->
                            GeoPoint(lngLatAlt[1], lngLatAlt[0])
                        }
                        Polyline().apply {
                            setPoints(geoPointsLine)
                            outlinePaint.strokeWidth = 6f
                            outlinePaint.color = Color.RED
                        }
                    })
                }

                else -> {
                    throw Exception("Invalid geometry type $geometryType")
                }
            }
        }
        return finiteSource
    }

    fun setDarkMode(darkMode: Boolean) {
        // TODO
    }

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
        private const val ZOOM_OUT_SPEED = 1000L
    }
}
