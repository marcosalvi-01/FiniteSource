package com.example.finitesource.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.openapitools.client.models.ConfigGeneralConfigJsonGet200Response
import kotlin.properties.Delegates

private const val CATALOG_CONFIG_TAG = "catalog_config"

private val Context.dataStore by preferencesDataStore(CATALOG_CONFIG_TAG)

object CatalogConfig {
	lateinit var ingvItUrl: String
	lateinit var ingvEnUrl: String
	lateinit var sourceGeoJsonFileName: String
	var slipColorPaletteVersion by Delegates.notNull<Int>()
	private lateinit var dataStore: DataStore<Preferences>

	suspend fun init(appContext: Context) {
		dataStore = appContext.dataStore
		dataStore.data.first().let { preferences ->
			ingvItUrl = preferences[ConfigKeys.INGV_IT_URL] ?: "http://terremoti.ingv.it/event/"
			ingvEnUrl = preferences[ConfigKeys.INGV_EN_URL] ?: "http://terremoti.ingv.it/en/event/"
			sourceGeoJsonFileName =
				preferences[ConfigKeys.SOURCE_GEOJSON_FILE_NAME] ?: "Finite_source.json"
			slipColorPaletteVersion =
				preferences[ConfigKeys.SLIP_COLOR_PALETTE_VERSION]?.toInt() ?: 1
		}
	}

	suspend fun save() {
		dataStore.edit { preferences ->
			preferences[ConfigKeys.INGV_IT_URL] = ingvItUrl
			preferences[ConfigKeys.INGV_EN_URL] = ingvEnUrl
			preferences[ConfigKeys.SOURCE_GEOJSON_FILE_NAME] = sourceGeoJsonFileName
			preferences[ConfigKeys.SLIP_COLOR_PALETTE_VERSION] = slipColorPaletteVersion.toString()
		}
	}

	suspend fun update(generalConfigResponse: ConfigGeneralConfigJsonGet200Response) {
		if (generalConfigResponse.serverIngvIt == null ||
			generalConfigResponse.serverIngvEn == null ||
			generalConfigResponse.sourceGeojson == null ||
			generalConfigResponse.paletteVersion == null
		)
			throw Exception("Error while updating config: some values are null")
		ingvItUrl = generalConfigResponse.serverIngvIt
		ingvEnUrl = generalConfigResponse.serverIngvEn
		sourceGeoJsonFileName = generalConfigResponse.sourceGeojson
		slipColorPaletteVersion = generalConfigResponse.paletteVersion

		save()
	}
}

object ConfigKeys {
	val INGV_IT_URL = stringPreferencesKey("ingv_it_url")
	val INGV_EN_URL = stringPreferencesKey("ingv_en_url")
	val SOURCE_GEOJSON_FILE_NAME = stringPreferencesKey("source_geojson_file_name")
	val SLIP_COLOR_PALETTE_VERSION = stringPreferencesKey("slip_color_palette_version")
}
