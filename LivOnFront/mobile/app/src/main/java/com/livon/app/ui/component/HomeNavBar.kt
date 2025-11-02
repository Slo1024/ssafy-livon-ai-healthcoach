package com.livon.app.ui.component.navbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R // 아이콘 리소스 참조를 위해 import
import com.livon.app.ui.theme.LivonTheme

// 내비게이션 라우트와 리소스를 정의하는 enum 클래스
enum class BottomNavRoute(
    val routeName: String,
    val title: String,
    val icon: Int,
    val iconFilled: Int
) {
    HOME(
        routeName = "home",
        title = "홈",
        icon = R.drawable.ic_home,
        iconFilled = R.drawable.ic_homefilled
    ),
    BOOKING(
        routeName = "booking",
        title = "예약하기",
        icon = R.drawable.ic_reservation,
        iconFilled = R.drawable.ic_reservationfilled
    ),
    RESERVATIONS(
        routeName = "reservations",
        title = "예약현황",
        icon = R.drawable.ic_check,
        iconFilled = R.drawable.ic_checkfilled
    ),
    MY_PAGE(
        routeName = "mypage",
        title = "마이페이지",
        icon = R.drawable.ic_personblack,
        iconFilled = R.drawable.ic_personblackfilled
    )
}

@Composable
fun HomeNavBar(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    onNavigate: (route: String) -> Unit,
) {
    val navItems = BottomNavRoute.values()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shadowElevation = 8.dp, // 약간의 그림자 효과
        color = MaterialTheme.colorScheme.surface // 기본 흰색 배경
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                // RowScope의 확장 함수로 각 아이템을 구현
                BottomNavItem(
                    item = item,
                    isSelected = currentRoute == item.routeName,
                    onClick = { onNavigate(item.routeName) }
                )
            }
        }
    }
}

// RowScope 내에서만 사용되도록 확장 함수로 정의
@Composable
private fun RowScope.BottomNavItem(
    item: BottomNavRoute,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconRes = if (isSelected) item.iconFilled else item.icon
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f) // 모든 아이템이 동일한 너비를 갖도록 설정
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = item.title,
            modifier = Modifier.size(24.dp),
            tint = contentColor
        )
        Text(
            text = item.title,
            fontSize = 10.sp,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeNavBarPreview() {
    LivonTheme {
        // "예약하기" 탭이 선택된 상태를 미리보기
        HomeNavBar(currentRoute = BottomNavRoute.BOOKING.routeName) {
            // Preview에서는 클릭 동작을 확인할 필요 없음
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeNavBarHomeSelectedPreview() {
    LivonTheme {
        // "홈" 탭이 선택된 상태를 미리보기
        HomeNavBar(currentRoute = BottomNavRoute.HOME.routeName) {}
    }
}
