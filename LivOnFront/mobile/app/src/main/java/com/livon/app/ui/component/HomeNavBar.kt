package com.livon.app.ui.component.navbar

import androidx.navigation.NavHostController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
//import androidx.compose.material.icons.filled.Event
import com.livon.app.R // 아이콘 리소스 참조를 위해 import
import com.livon.app.ui.theme.LivonTheme

enum class BottomNavRoute(
    val routeName: String,
    val title: String,
    val icon: Int
) {
    HOME(
        routeName = "home",
        title = "홈",
        icon = R.drawable.ic_home
    ),
    BOOKING(
        routeName = "booking",
        title = "예약하기",
        icon = R.drawable.ic_reservation
    ),
    RESERVATIONS(
        routeName = "reservations",
        title = "예약현황",
        icon = R.drawable.ic_check
    ),
    MY_PAGE(
        routeName = "mypage",
        title = "마이페이지",
        icon = R.drawable.ic_personblack
    )
}

@Composable
fun HomeNavBar(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    navController: NavHostController? = null,
    onNavigate: (route: String) -> Unit,
) {
    val navItems = BottomNavRoute.values()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp), // 높이 증가
        shadowElevation = 8.dp, // 약간의 그림자 효과
        color = MaterialTheme.colorScheme.surface // 기본 흰색 배경
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                BottomNavItem(
                    item = item,
                    isSelected = currentRoute == item.routeName,
                    navController = navController,
                    onClick = { route ->
                        // If navController provided, HomeNavBar will perform default navigation mapping.
                        if (navController != null) {
                            when (route) {
                                BottomNavRoute.HOME.routeName -> navController.navigate(com.livon.app.navigation.Routes.MemberHome)
                                BottomNavRoute.BOOKING.routeName -> navController.navigate(com.livon.app.navigation.Routes.ReservationModeSelect)
                                BottomNavRoute.RESERVATIONS.routeName -> navController.navigate(com.livon.app.navigation.Routes.Reservations)
                                BottomNavRoute.MY_PAGE.routeName -> navController.navigate(com.livon.app.navigation.Routes.MyPage)
                                else -> { /* noop */ }
                            }
                        } else {
                            onNavigate(route)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    item: BottomNavRoute,
    isSelected: Boolean,
    navController: NavHostController? = null,
    onClick: (String) -> Unit
) {
    val iconRes = item.icon
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black

    Column(
        modifier = Modifier
            .padding(top = 8.dp) // 아이콘 위 여백 추가
            .fillMaxHeight()
            .weight(1f) // 모든 아이템이 동일한 너비를 갖도록 설정
            .clickable(onClick = { onClick(item.routeName) }),
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
        HomeNavBar(currentRoute = BottomNavRoute.BOOKING.routeName, onNavigate = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeNavBarHomeSelectedPreview() {
    LivonTheme {
        HomeNavBar(currentRoute = BottomNavRoute.HOME.routeName, onNavigate = {})
    }
}


@Composable
private fun TestNavBarNoResources(currentRoute: String?, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp), // 테스트용도도 동일 높이
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(
            Triple("home", "홈", Icons.Default.Home),
//            Triple("booking", "예약하기", Icons.Default.Event),
            Triple("reservations", "예약현황", Icons.Default.Check),
            Triple("mypage", "마이페이지", Icons.Default.Person)
        )
        items.forEach { (route, title, icon) ->
            val selected = currentRoute == route
            Column(
                modifier = Modifier
                    .padding(top = 8.dp) // 아이콘 위 여백 추가
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onNavigate(route) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = icon, contentDescription = title)
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
