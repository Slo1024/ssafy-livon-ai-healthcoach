package com.livon.app.feature.shared.streaming

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.livon.app.feature.coach.streaming.ui.ConfigureUrlsScreen

class ConfigureUrlsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialServerUrl = Urls.applicationServerUrl
        val initialLivekitUrl = Urls.livekitUrl


        setContent {
            ConfigureUrlsScreen(
                initialServerUrl = Urls.applicationServerUrl,
                initialLivekitUrl = Urls.livekitUrl,
                onSaveUrls = ::onSaveUrls
            )
        }
    }

    private fun onSaveUrls(serverUrl: String, livekitUrl: String) {
        if (serverUrl.isNotEmpty() && livekitUrl.isNotEmpty()) {
            Urls.applicationServerUrl = serverUrl
            Urls.livekitUrl = livekitUrl
            finish()
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}