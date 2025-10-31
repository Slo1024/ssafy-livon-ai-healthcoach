package com.livon.app.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.livon.app.feature.member.home.ui.MemberHomeRoute

fun NavGraphBuilder.memberNavGraph(nav: NavHostController) {

    /** ✅ 회원 메인 **/
    composable("member_home") {
        MemberHomeRoute(
            onTapBooking = { nav.navigate("booking") },
            onTapReservations = { nav.navigate("reservations") }
        )
    }

    /** ✅ 예약하기 화면 (임시) **/
    composable("booking") {
        SimplePlaceholder(title = "예약하기")
    }

    /** ✅ 예약 현황 화면 (임시) **/
    composable("reservations") {
        SimplePlaceholder(title = "예약 현황")
    }
}

@Composable
private fun SimplePlaceholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title 화면입니다.")
    }
}
