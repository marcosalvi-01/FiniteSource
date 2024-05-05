package com.example.finitesource.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.finitesource.*
import com.example.finitesource.databinding.ActivityInfoBinding
import java.util.*

class InfoActivity : AppCompatActivity() {
	private val binding by lazy {
		ActivityInfoBinding.inflate(layoutInflater)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		// set the toolbar
		setSupportActionBar(binding.toolbar)

		// set the listener for the back button
		binding.toolbar.setNavigationOnClickListener {
			// finish the activity when the back button is pressed
			finish()
		}

		// if is light theme, set the status bar to light
		if (!isDarkTheme(this))
			lightStatusBar(window, true)

		// Open the URL when the button is clicked
		binding.visitWebsiteButton.setOnClickListener {
			val url = if (Locale.getDefault().language == "it") INGV_URL_IT else INGV_URL_EN
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
		}

		// Open on GitHub
		binding.openOnGithubButton.setOnClickListener {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
		}

		// Set the version
		val packageInfo = packageManager.getPackageInfo(packageName, 0)
		binding.versionInfo.text = getString(
			R.string.version_format,
			getString(R.string.app_name),
			packageInfo.versionName
		)
	}
}