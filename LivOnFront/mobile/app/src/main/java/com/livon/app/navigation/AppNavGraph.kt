package com.livon.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = "member_home"
    ) {
        memberNavGraph(nav)
        coachNavGraph(nav) // 지금은 비워둬도 OK
    }
}
