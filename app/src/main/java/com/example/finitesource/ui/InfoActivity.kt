package com.example.finitesource.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.finitesource.R
import com.example.finitesource.databinding.ActivityInfoBinding
import com.example.finitesource.isDarkTheme
import com.example.finitesource.lightStatusBar
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class InfoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityInfoBinding.inflate(layoutInflater)
    }

    // Replace this with your actual endpoint:
    private val ENDPOINT_URL = "https://finitesource.ingv.it/v3/config/AppInfo.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Set up toolbar & back navigation
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // If light theme, set the status bar to light
        if (!isDarkTheme(this)) {
            lightStatusBar(window, true)
        }

        // Set the version
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        binding.versionInfo.text = getString(
            R.string.version_format,
            getString(R.string.app_name),
            packageInfo.versionName
        )

        // Now fetch the JSON from remote endpoint
        fetchInfoData()
    }

    private fun fetchInfoData() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(ENDPOINT_URL)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle error properly in production (e.g., show a Snackbar, etc.)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        // Handle error (show message, etc.)
                        return
                    }
                    val responseBody = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(responseBody)

                    // Update UI on main thread
                    runOnUiThread {
                        displayData(jsonArray)
                    }
                }
            }
        })
    }

    private fun displayData(jsonArray: JSONArray) {
        // Determine which language to show: "it" or "en"
        val localeLang = if (Locale.getDefault().language == "it") "it" else "en"

        // The container in activity_info.xml where we add dynamic views
        val container = binding.dynamicContainer

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)

            // 1) Title
            val titleTextView = createTitleTextView(item, localeLang, container)
            titleTextView?.let {
                container.addView(it)
            }

            // 2) Content
            val contentTextView = createContentTextView(item, localeLang, container)
            contentTextView?.let {
                container.addView(it)
            }

            // 3) Button
            val buttonView = createButtonView(item, localeLang, container)
            buttonView?.let {
                container.addView(it)
            }

            container.addView(createSpaceView(container))
        }
    }

    private fun createSpaceView(parent: ViewGroup): Space {
        return layoutInflater.inflate(R.layout.info_item_spacer, parent, false) as Space
    }

    /**
     * Inflate and return a TextView for the title (if it exists).
     */
    private fun createTitleTextView(item: JSONObject, lang: String, parent: ViewGroup): TextView? {
        // "title" might be missing or null
        if (!item.has("title") || item.isNull("title")) return null
        val titleObj = item.getJSONObject("title")
        val titleString = titleObj.optString(lang, "").trim()
        if (titleString.isBlank()) return null

        // Inflate the "info_item_title.xml" layout, which is just a TextView
        val titleView = layoutInflater.inflate(R.layout.info_item_title, parent, false) as TextView
        titleView.text = titleString
        return titleView
    }

    /**
     * Inflate and return a TextView for the content (if it exists).
     */
    private fun createContentTextView(
        item: JSONObject,
        lang: String,
        parent: ViewGroup
    ): TextView? {
        if (!item.has("content") || item.isNull("content")) return null
        val contentObj = item.getJSONObject("content")
        val contentString = contentObj.optString(lang, "").trim()
        if (contentString.isBlank()) return null

        val contentView =
            layoutInflater.inflate(R.layout.info_item_content, parent, false) as TextView
        contentView.text = contentString
        return contentView
    }

    /**
     * Inflate and return a Button if "button" is present (for the current lang).
     */
    private fun createButtonView(item: JSONObject, lang: String, parent: ViewGroup): Button? {
        if (!item.has("button") || item.isNull("button")) return null
        val buttonObj = item.getJSONObject("button")
        val subObj = buttonObj.optJSONObject(lang) ?: return null

        val buttonText = subObj.optString("text", "").trim()
        val buttonLink = subObj.optString("link", "").trim()
        if (buttonText.isBlank() || buttonLink.isBlank()) return null

        // Inflate the "info_item_button.xml"
        val buttonView = layoutInflater.inflate(R.layout.info_item_button, parent, false) as Button
        buttonView.text = buttonText
        buttonView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(buttonLink))
            startActivity(intent)
        }
        return buttonView
    }
}
