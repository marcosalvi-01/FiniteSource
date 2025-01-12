package com.example.finitesource

import okhttp3.OkHttpClient

// Intent keys
const val IMAGE_URL_KEY = "image_url"
const val TITLE_ID_KEY = "title_id"

// GeoJson properties keys
const val SLIP_KEY = "Slip_m"
const val RAKE_KEY = "Rake_d"

// okhttp client
val okHttpClient = OkHttpClient().newBuilder().build()

val configUrl = "https://finitesource.ingv.it/v3/config/GeneralConfig.json"
val providersUrl = "https://finitesource.ingv.it/v3/config/FocalMechanismProviders.json"
val paletteUrl = "https://finitesource.ingv.it/v3/config/SlipColorPalette.json"

// INGV URL
const val INGV_URL_IT = "https://terremoti.ingv.it/finitesource"
const val INGV_URL_EN = "https://terremoti.ingv.it/en/finitesource"
const val GITHUB_URL = "https://github.com/salvi-1883208/FiniteSource"