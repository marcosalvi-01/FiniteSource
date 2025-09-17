package it.ingv.finitesource.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlin.properties.Delegates

private const val CATALOG_CONFIG_TAG = "catalog_config"

private val Context.dataStore by preferencesDataStore(CATALOG_CONFIG_TAG)

object CatalogConfig {
	lateinit var ingvItUrl: String
	lateinit var ingvEnUrl: String
	var slipColorPaletteVersion by Delegates.notNull<Int>()
	lateinit var slipColorPalette: SlipColorPalette
	private lateinit var dataStore: DataStore<Preferences>

	suspend fun init(appContext: Context) {
		dataStore = appContext.dataStore
		dataStore.data.first().let { preferences ->
			ingvItUrl = preferences[ConfigKeys.INGV_IT_URL] ?: "http://terremoti.ingv.it/event/"
			ingvEnUrl = preferences[ConfigKeys.INGV_EN_URL] ?: "http://terremoti.ingv.it/en/event/"
			slipColorPaletteVersion =
				preferences[ConfigKeys.SLIP_COLOR_PALETTE_VERSION]?.toInt() ?: 0
			slipColorPalette = preferences[ConfigKeys.SLIP_COLOR_PALETTE]?.let {
				SlipColorPalette.fromString(it)
			}
				?: SlipColorPalette()
		}
	}

	suspend fun save() {
		dataStore.edit { preferences ->
			preferences[ConfigKeys.INGV_IT_URL] = ingvItUrl
			preferences[ConfigKeys.INGV_EN_URL] = ingvEnUrl
			preferences[ConfigKeys.SLIP_COLOR_PALETTE_VERSION] = slipColorPaletteVersion.toString()
			preferences[ConfigKeys.SLIP_COLOR_PALETTE] = slipColorPalette.toString()
		}
	}

	/**
	 * Updates the config and returns true if the slip color palette has been updated.
	 * It also saves the new updated config.
	 */
	suspend fun update(serverIngvEn: String?, serverIngvIt: String?, paletteVersion: Int?): Boolean {
		if (serverIngvIt == null ||
			serverIngvEn == null ||
			paletteVersion == null
		)
			throw Exception("Error while updating config: some values are null")
		// update the values
		ingvItUrl = serverIngvIt
		ingvEnUrl = serverIngvEn

		// check if the slip color palette has been updated
//		if (slipColorPaletteVersion != paletteVersion) {
			// update the slip color palette version
			slipColorPaletteVersion = paletteVersion
			// return true to signal that the slip color palette has been updated
			return true
//		}
		// return false to signal that the slip color palette has not been updated
		save()
		return false
	}

	/**
	 * Updates the slip color palette and saves it.
	 */
	suspend fun updateSlipColorPalette(slipColorPaletteResponse: List<String>) {
		this.slipColorPalette = SlipColorPalette.fromList(slipColorPaletteResponse)
		save()
	}
}

object ConfigKeys {
	val INGV_IT_URL = stringPreferencesKey("ingv_it_url")
	val INGV_EN_URL = stringPreferencesKey("ingv_en_url")
	val SOURCE_GEOJSON_FILE_NAME = stringPreferencesKey("source_geojson_file_name")
	val SLIP_COLOR_PALETTE_VERSION = stringPreferencesKey("slip_color_palette_version")
	val SLIP_COLOR_PALETTE = stringPreferencesKey("slip_color_palette")
}
