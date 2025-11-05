// kotlin
package com.livon.app.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.livon.app.feature.shared.auth.ui.BirthdayScreen
import com.livon.app.feature.shared.auth.ui.CompanySelectScreen
import com.livon.app.feature.shared.auth.ui.EmailLoginScreen
import com.livon.app.feature.shared.auth.ui.EmailSetupScreen
import com.livon.app.feature.shared.auth.ui.EmailVerifyScreen
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
import com.livon.app.feature.shared.auth.ui.NicknameScreen
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
    const val LifeStyleSurvey = "life_style_survey"

    const val MemberHome = "member_home"
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Routes.Landing
    ) {
        authNavGraph(nav)
        memberNavGraph(nav)
        coachNavGraph(nav)
    }
}

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    composable(Routes.Landing) {
        LandingScreen(
            onKakaoLogin = {},
            onNaverLogin = {},
            onEmailLogin = { Log.d("AppNavGraph","navigate to EmailLogin"); navController.navigate(Routes.EmailLogin) },
            onSignUp = { Log.d("AppNavGraph","navigate to Terms"); navController.navigate(Routes.Terms) }
        )
    }

    composable(Routes.Terms) {
        TermOfUseScreen(
            onClickNext = { Log.d("AppNavGraph","Terms -> RoleSelect"); navController.navigate(Routes.RoleSelect) },
            onClickBack = { navController.popBackStack() }
        )
    }

    composable(Routes.RoleSelect) {
        RoleSelectScreen(
            modifier = androidx.compose.ui.Modifier,
            onBack = { navController.popBackStack() },
            onComplete = { mode -> Log.d("AppNavGraph","RoleSelect -> EmailSetup"); navController.navigate(Routes.EmailSetup) }
        )
    }

    composable(Routes.EmailLogin) {
        EmailLoginScreen(
            onBack = { navController.popBackStack() },
            onLogin = { email, pw -> /* TODO */ },
            onSignUp = { Log.d("AppNavGraph","navigate to Terms"); navController.navigate(Routes.Terms) },
            onFindId = {},
            onFindPassword = {}
        )
    }

    composable(Routes.EmailSetup) {
        EmailSetupScreen(
            modifier = androidx.compose.ui.Modifier,
            onBack = { navController.popBackStack() },
            onNext = { email -> Log.d("AppNavGraph","EmailSetup -> EmailVerify (email=$email)"); navController.navigate(Routes.EmailVerify) }
        )
    }
    composable(Routes.EmailVerify) {
        EmailVerifyScreen(
            modifier = androidx.compose.ui.Modifier,
            onBack = { navController.popBackStack() },
            onVerified = {
                Log.d("AppNavGraph","EmailVerify -> NickName (verified)")
                navController.navigate(Routes.NickName)
            }
        )
    }

    composable(Routes.NickName) {
        NicknameScreen(
            onBack = { navController.popBackStack() },
            onNext = { nickname ->
                Log.d("AppNavGraph","NickName -> MemberTypeSelect (nickname=$nickname)")
                navController.navigate(Routes.MemberTypeSelect)
            }
        )
    }

    composable(Routes.MemberTypeSelect) {
        MemberTypeSelectScreen(
            onBack = { navController.popBackStack() },
            onComplete = { mode -> Log.d("AppNavGraph","MemberTypeSelect -> next (mode=$mode)"); if (mode == "business") navController.navigate(Routes.CompanySelect) else navController.navigate(Routes.GenderSelect) }
        )
    }

    composable(Routes.CompanySelect) { CompanySelectScreen(onBack = { navController.popBackStack() }, onNext = { Log.d("AppNavGraph","CompanySelect -> GenderSelect"); navController.navigate(Routes.GenderSelect) }) }

    composable(Routes.GenderSelect) {
        GenderSelectScreen(
            onBack = { navController.popBackStack() },
            onNext = { gender -> Log.d("AppNavGraph","GenderSelect -> Birthday (gender=$gender)"); navController.navigate(Routes.Birthday) }
        )
    }
    composable(Routes.Birthday) {
        BirthdayScreen(
            onBack = { navController.popBackStack() },
            onNext = { year, month, day ->
                Log.d("AppNavGraph","Birthday -> ProfilePhoto (y=$year,m=$month,d=$day)")
                navController.navigate(Routes.ProfilePhoto)
            }
        )
    }
    composable(Routes.ProfilePhoto) { ProfilePhotoSelectScreen(onBack = { navController.popBackStack() }, onComplete = { Log.d("AppNavGraph","ProfilePhoto -> signup_complete"); navController.navigate("signup_complete?username=%EC%99%95%EB%9D%BC%EB%B9%84") }) }

    composable(route = "signup_complete?username={username}", arguments = listOf(navArgument("username") { type = NavType.StringType; defaultValue = "" })) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("username") ?: ""
        val username = try { URLDecoder.decode(encoded, "UTF-8") } catch (t: Throwable) { encoded }
        SignUpCompleteScreen(username = if (username.isBlank()) "회원" else username, onStart = { Log.d("AppNavGraph","SignUpComplete -> MemberHome"); navController.navigate(Routes.MemberHome) })
    }

    composable(Routes.HealthHeight) { HealthInfoHeightScreen() }
    composable(Routes.HealthWeight) { HealthInfoWeightScreen() }
    composable(Routes.HealthSurvey) { HealthInfoHeightScreen() }
    composable(Routes.LifeStyleSurvey) { HealthInfoHeightScreen() }
}
