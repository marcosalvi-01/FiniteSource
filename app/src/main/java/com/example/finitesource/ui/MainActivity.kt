package com.example.finitesource.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finitesource.R
import com.example.finitesource.data.local.CatalogConfig
import com.example.finitesource.databinding.ActivityMainBinding
import com.example.finitesource.databinding.LegendBottomSheetBinding
import com.example.finitesource.isDarkTheme
import com.example.finitesource.lightStatusBar
import com.example.finitesource.ui.mapoverlays.SlipPalette
import com.example.finitesource.viewmodels.EarthquakesViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

	private val earthquakesViewModel: EarthquakesViewModel by viewModels()
	private val binding: ActivityMainBinding by lazy {
		ActivityMainBinding.inflate(layoutInflater)
	}

	// TODO place this in the xml
	private val slipPaletteView by lazy { SlipPalette(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// load the osmdroid configuration
		Configuration.getInstance()
			.load(
				applicationContext,
				PreferenceManager.getDefaultSharedPreferences(applicationContext)
			)
		// inflate the layout and set it as the content view
		setContentView(binding.root)

		// set the toolbar as the default action bar
		setSupportActionBar(binding.toolbar)
		supportActionBar?.hide()

		// change the color of the status bar to match the theme
		lightStatusBar(window, true)

		// give the viewmodel to the mapview
		binding.customMapView.earthquakesViewModel = earthquakesViewModel

		// set up the various map overlays
		mapOverlaysInit()

		// make the status bar transparent
		transparentStatusBar()

		// set up the search view and bar
		searchInit()

//		// load the config
//		// TODO block the app until the config and the earthquake data are loaded (show a loading screen)
		lifecycleScope.launch {
			CatalogConfig.init(this@MainActivity)
		}

		// do something with the updates
		earthquakesViewModel.getUpdates().observe(this) {
			if (it == null)
				Toast.makeText(
					this@MainActivity,
					"Failed to update earthquakes",    // TODO: use snackbar
					Toast.LENGTH_SHORT
				).show()
			Log.d("MainActivity", "Updates: $it")
		}
//
		earthquakesViewModel.earthquakes.observe(this) { earthquakes ->
			Log.d("MainActivity", "Earthquakes: ${earthquakes.size}")
			if (earthquakes.isNotEmpty()) {
				binding.customMapView.setEarthquakes(earthquakes)
			} else {
				// show a loading screen
			}
		}

		earthquakesViewModel.uiState.observe(this) {
			Log.d("MainActivity", "Selected earthquake: $it")
		}
	}

	/**
	 * Initializes the map view overlays.
	 *
	 * See [compassButtonInit], [legendButtonInit], [focalPlaneSwitchInit] and [slipAlphaSliderInit].
	 */
	private fun mapOverlaysInit() {
		// TODO fix the whole overlay system
		// right now it is a mess of views and dependencies, it is not scalable and it is not
		// easy to understand
		binding.customMapView.compassButton = binding.compassButton
		legendButtonInit()
		// add the slip palette
		binding.mainContent.addView(slipPaletteView, 3)
		focalPlaneSwitchInit()
		slipAlphaSliderInit()
	}

	/**
	 * Initializes the focal plane switch.
	 */
	private fun focalPlaneSwitchInit() {
		// set the listener for the focal plane switch
		binding.focalPlaneSwitch.setOnCheckedChangeListener { _, isChecked ->
			// call the viewModel
//			focalPlaneSwitchViewModel.onCheckedChanged(isChecked)
		}
	}

	/**
	 * Initializes the legend button.
	 */
	private fun legendButtonInit() {
		// Create a bottom sheet dialog for the legend
		val legendBottomSheetDialog = BottomSheetDialog(this)

		// Set a click listener for the legend button
		binding.legendButton.setOnClickListener {
			// Inflate the layout for the bottom sheet
			val legendBottomSheetBinding = LegendBottomSheetBinding.inflate(layoutInflater)
			legendBottomSheetDialog.setContentView(legendBottomSheetBinding.root)

			// Change the color of the event icon for the "No Finite Source" event
			val noFiniteSourceIcon = ResourcesCompat.getDrawable(
				resources,
				R.drawable.event_circle_icon,
				theme
			)?.mutate() as LayerDrawable
			noFiniteSourceIcon.findDrawableByLayerId(R.id.background).setTint(
				getColor(R.color.no_finite_source)
			)

			// Set the modified icon for the "No Finite Source" event
			legendBottomSheetBinding.legendNoFiniteSourceIcon.setImageDrawable(noFiniteSourceIcon)

			// Show the legend bottom sheet dialog
			legendBottomSheetDialog.show()
		}
	}

	/**
	 * Initializes the slip alpha slider.
	 */
	private fun slipAlphaSliderInit() {
		// set the listener for the slip alpha slider
		binding.slipAlphaSlider.setOnSeekBarChangeListener(object :
			SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				// post an event to the event channel
//				EventBus.postEvent(Event(SLIP_ALPHA_SLIDER_SLIDE, 255 - progress))
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {}
			override fun onStopTrackingTouch(p0: SeekBar?) {}
		})
	}

	/**
	 * Initializes the search view, the global list of events and the navigation drawer.
	 */
	private fun searchInit() {
		// Initialize the global list
		globalListInit()

		// Initialize the navigation drawer
		navigationDrawerInit()

		// Retry button click listener for downloading global events again
		binding.searchErrorRetryButton.setOnClickListener {
//			searchViewModel.loadGlobalEvents()
		}

		// Set the icon color
		binding.searchBar.menu.findItem(R.id.navigation_drawer_item).iconTintList =
			ColorStateList.valueOf(getColor(R.color.on_background))

		// Set the tag to false to indicate that the search bar is not showing the back arrow
		binding.searchBar.tag = false

		// Navigation drawer icon click listener
		binding.searchBar.setOnMenuItemClickListener {
			when (it.itemId) {
				R.id.navigation_drawer_item -> {
					// Open the navigation drawer
					binding.navigationDrawer.openDrawer(GravityCompat.START)
					true
				}

				else -> false
			}
		}

		// Observe changes in the global list
//		globalListViewModel.globalListItemLiveData.observe(this) { list ->
//			if (list == null) {
//				// Hide the global list and show the error container
//				binding.searchResults.visibility = View.GONE
//				binding.searchErrorContainer.visibility = View.VISIBLE
//			} else {
//				// Show the global list and hide the error container
//				binding.searchResults.visibility = View.VISIBLE
//				binding.searchErrorContainer.visibility = View.GONE
//			}
//		}

		// Listen to changes in the search view visibility
		binding.searchView.viewTreeObserver.addOnGlobalLayoutListener {
			// If the bottom sheet is not expanded update the search view visibility state
//			if (!binding.persistentBottomSheet.isExpanded()) {
//				val isCurrentlyShown =
//					binding.searchResults.isShown || binding.searchErrorContainer.isShown
//				if (isCurrentlyShown != isSearchViewShown) {
//					isSearchViewShown = isCurrentlyShown
//					val eventType = if (isSearchViewShown) SEARCH_VIEW_SHOWN else SEARCH_VIEW_HIDDEN
			// Post the event to the event channel
//					EventBus.postEvent(Event(eventType))
//				}
//			}
		}

		// Listen to text changes in the search view
		binding.searchView.editText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable?) {
				// Post an event to the event channel with the updated search query
//				EventBus.postEvent(Event(SEARCH_VIEW_TEXT_CHANGED, s.toString()))
			}

			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
		})
	}

	/**
	 * Initializes the global list of events inside the search view.
	 */
	private fun globalListInit() {
		// Set up the RecyclerView and its adapter
		val layoutManager = LinearLayoutManager(this)
		layoutManager.orientation = LinearLayoutManager.VERTICAL
		binding.searchResults.layoutManager = layoutManager

		// Attach the adapter to the RecyclerView and pass the viewModel
//		binding.searchResults.adapter = GlobalListAdapter(globalListViewModel, this)

		binding.searchResults.isVerticalScrollBarEnabled = true

		// Hide the keyboard when the RecyclerView is scrolled
		binding.searchResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				// TODO hide the keyboard when the user touches the list
				// not just when the list is scrolled
				if (dy != 0) {
					val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
					imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
				}
			}
		})
	}

	/**
	 * Initializes the navigation drawer.
	 */
	private fun navigationDrawerInit() {
		// Navigation drawer item click listener
		binding.navigationView.setNavigationItemSelectedListener { menuItem ->
			when (menuItem.itemId) {
				R.id.info_item -> {
					// TODO: Implement the info activity
					true
				}

				else -> false
			}
		}

		// Lock the navigation drawer to prevent opening it with a swipe
		binding.navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

		// Drawer listener for handling drawer open/close events
		binding.navigationDrawer.addDrawerListener(object : DrawerLayout.DrawerListener {
			override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
			override fun onDrawerOpened(drawerView: View) {
				// Change status bar color when drawer is opened
				lightStatusBar(window, !isDarkTheme(this@MainActivity))
			}

			override fun onDrawerClosed(drawerView: View) {
				// Change status bar color when drawer is closed
				lightStatusBar(window, true)
			}

			override fun onDrawerStateChanged(newState: Int) {}
		})
	}


	private fun transparentStatusBar() {
		// TODO don't use deprecated methods
		window.decorView.systemUiVisibility =
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		window.statusBarColor = Color.TRANSPARENT
	}
}


