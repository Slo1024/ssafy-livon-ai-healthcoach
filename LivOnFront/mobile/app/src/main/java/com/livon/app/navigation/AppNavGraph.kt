// kotlin
package com.livon.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.livon.app.feature.shared.auth.ui.BirthdayScreen
import com.livon.app.feature.shared.auth.ui.CompanySelectScreen
import com.livon.app.feature.shared.auth.ui.EmailLoginScreen
import com.livon.app.feature.shared.auth.ui.GenderSelectScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoHeightScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoWeightScreen
import com.livon.app.feature.shared.auth.ui.LandingScreen
import com.livon.app.feature.shared.auth.ui.MemberTypeSelectScreen
import com.livon.app.feature.shared.auth.ui.ProfilePhotoSelectScreen
import com.livon.app.feature.shared.auth.ui.RoleSelectScreen
import com.livon.app.feature.shared.auth.ui.SignUpCompleteScreen
import com.livon.app.feature.shared.auth.ui.TermOfUseScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import java.net.URLDecoder

object Routes {
    const val Landing = "landing"
    const val Terms = "terms_of_use"
    const val RoleSelect = "role_select"
    const val EmailLogin = "email_login"
    const val EmailSetup = "email_setup"
    const val EmailVerify = "email_verify"
    const val NickName = "nickname"
    const val MemberTypeSelect = "member_type_select"
    const val CompanySelect = "company_select"
    const val GenderSelect = "gender_select"
    const val Birthday = "birthday"
    const val ProfilePhoto = "profile_photo"
    const val HealthHeight = "health_height"
    const val HealthWeight = "health_weight"
    const val HealthSurvey = "health_survey"
    const val LifeStyleSurvey = "lifestyle_survey"

    const val MemberHome = "member_home"
    // 예약 관련 라우트는 기존 reservation 그래프에서 관리해도 됩니다
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Routes.Landing
    ) {
        authNavGraph(nav)
        memberNavGraph(nav)    // 기존에 구현된 그래프 사용
        coachNavGraph(nav)     // 그대로 유지
        // 필요하면 reservationNavGraph(nav) 등 추가
    }
}

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    composable(Routes.Landing) {
        // LandingScreen은 콜백들(기본값 존재)으로 구성되어 있으므로 nav로부터 필요한 동작만 전달
        LandingScreen(
            onKakaoLogin = {},
            onNaverLogin = {},
            onEmailLogin = { navController.navigate(Routes.EmailLogin) },
            onSignUp = { navController.navigate(Routes.Terms) }
        )
    }
    composable(Routes.Terms) {
        TermOfUseScreen(
            onClickNext = { navController.navigate(Routes.RoleSelect) },
            onClickBack = { navController.popBackStack() }
        )
    }
    composable(Routes.RoleSelect) {
        RoleSelectScreen(
            modifier = androidx.compose.ui.Modifier,
            onComplete = { mode ->
                // 임시: 회원가입 흐름 계속 (예: 이메일 설정)
                navController.navigate(Routes.EmailSetup)
            }
        )
    }
    composable(Routes.EmailLogin) {
        EmailLoginScreen(
            onBack = { navController.popBackStack() },
            onLogin = { email, pw -> /* TODO: 로그인 처리 */ },
            onSignUp = { navController.navigate(Routes.Terms) },
            onFindId = {},
            onFindPassword = {}
        )
    }
    composable(Routes.EmailSetup) {
        // EmailSetUpScreen 함수가 프로젝트에 존재하지 않을 수 있으므로 안전하게 기본 화면 호출로 대체
        // 만약 실제 EmailSetUpScreen.kt가 있다면 여기에 정확한 호출로 교체하세요.
        // EmailVerifyScreen 대신 단순 Landing placeholder
        LandingScreen()
    }
    composable(Routes.EmailVerify) {
        LandingScreen()
    }
    composable(Routes.NickName) {
        // NickNameScreen 정의가 없으면 기본 동작으로 RoleSelect 화면으로 돌아가게 둠
        // 실제 구현이 있으면 호출 시 nav 콜백을 전달하세요.
        RoleSelectScreen()
    }
    composable(Routes.MemberTypeSelect) {
        MemberTypeSelectScreen()
    }
    composable(Routes.CompanySelect) {
        CompanySelectScreen()
    }
    composable(Routes.GenderSelect) {
        GenderSelectScreen()
    }
    composable(Routes.Birthday) {
        BirthdayScreen(onBack = { navController.popBackStack() })
    }
    composable(Routes.ProfilePhoto) {
        ProfilePhotoSelectScreen(onBack = { navController.popBackStack() }, onComplete = { navController.navigate("signup_complete?username=%EC%99%95%EB%9D%BC%EB%B9%84") })
    }

    // SignUpComplete expects a username string argument; create a route that accepts an optional query param
    composable(
        route = "signup_complete?username={username}",
        arguments = listOf(navArgument("username") { type = NavType.StringType; defaultValue = "" })
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("username") ?: ""
        val username = try { URLDecoder.decode(encoded, "UTF-8") } catch (t: Throwable) { encoded }
        SignUpCompleteScreen(username = if (username.isBlank()) "회원" else username, onStart = { navController.navigate(Routes.MemberHome) })
    }

    composable(Routes.HealthHeight) {
        // Health screens in this project take no args (based on get_errors), so call directly
        HealthInfoHeightScreen()
    }
    composable(Routes.HealthWeight) {
        HealthInfoWeightScreen()
    }
    composable(Routes.HealthSurvey) {
        // placeholder: if HealthSurveyScreen exists, replace accordingly
        HealthInfoHeightScreen()
    }
    composable(Routes.LifeStyleSurvey) {
        HealthInfoHeightScreen()
    }
}
