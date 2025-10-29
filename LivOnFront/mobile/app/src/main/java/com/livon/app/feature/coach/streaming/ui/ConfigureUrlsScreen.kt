package com.livon.app.feature.coach.streaming.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun ConfigureUrlsScreen(
    initialServerUrl: String,
    initialLivekitUrl: String,
    onSaveUrls: (serverUrl: String, livekitUrl: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var serverUrl by remember { mutableStateOf(initialServerUrl) }
    var livekitUrl by remember { mutableStateOf(initialLivekitUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.configure_urls),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 48.dp, bottom = 20.dp)
        )

        Text(
            text = stringResource(id = R.string.application_server_url),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 16.dp)
        )
        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("Application Server URL") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.livekit_url),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 16.dp)
        )
        OutlinedTextField(
            value = livekitUrl,
            onValueChange = { livekitUrl = it },
            label = { Text("LiveKit URL") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        )

        Button(
            onClick = {
                onSaveUrls(serverUrl, livekitUrl)
            },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(stringResource(id = R.string.save), fontWeight = FontWeight.Bold) // @string/save
        }
    }
}