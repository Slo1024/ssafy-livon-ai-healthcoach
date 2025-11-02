package com.livon.app.feature.member.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MemberHomeRoute(
    onTapBooking: () -> Unit,
    onTapReservations: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("회원 홈")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onTapBooking) { Text("예약하기") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onTapReservations) { Text("예약 현황") }
    }
}
