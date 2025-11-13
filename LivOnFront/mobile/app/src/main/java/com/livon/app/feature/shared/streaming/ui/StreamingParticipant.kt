package com.livon.app.feature.shared.streaming.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.livon.app.ui.component.streaming.SearchBar
import com.livon.app.ui.component.streaming.StreamingParticipantsHeader
import com.livon.app.ui.component.streaming.UserProfileItem
import com.livon.app.ui.theme.LivonTheme

@Composable
fun StreamingParticipant(
    onBackClick: () -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LivonTheme {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .zIndex(1f)
                ) {
                    StreamingParticipantsHeader(onBackClick = onBackClick)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchBar(
                    onSearch = onSearch,
                    modifier = Modifier.fillMaxWidth()
                )
                UserProfileItem(
                    userName = "참가자 A",
                )
            }
        }
    }
}
