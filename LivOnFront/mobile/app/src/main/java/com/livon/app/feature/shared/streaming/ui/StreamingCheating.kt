package com.livon.app.feature.shared.streaming.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livon.app.ui.component.streaming.CheatingBar
import com.livon.app.ui.component.streaming.SearchBar
import com.livon.app.ui.component.streaming.StreamingCheatingHeader
import com.livon.app.ui.component.streaming.StreamingCheatingProfile
import com.livon.app.ui.component.streaming.StreamingParticipantsHeader
import com.livon.app.ui.theme.LivonTheme

@Composable
fun StreamingCheating(
    onBackClick: () -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LivonTheme {
        Scaffold(
            topBar = {
                StreamingCheatingHeader(onBackClick = onBackClick)
            },
            bottomBar = {
                CheatingBar(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                StreamingCheatingProfile(
                    userName = "홍길동",
                    message = "안녕하세요",
                    time = "14:30"
                )
            }
        }
    }
}
