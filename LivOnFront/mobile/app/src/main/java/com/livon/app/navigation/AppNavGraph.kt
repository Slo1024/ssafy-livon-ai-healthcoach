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
import com.livon.app.feature.shared.auth.ui.PasswordSetupScreen
import com.livon.app.feature.shared.auth.ui.ProfilePhotoSelectScreen
import com.livon.app.feature.shared.auth.ui.RoleSelectScreen
import com.livon.app.feature.shared.auth.ui.SignUpCompleteScreen
import com.livon.app.feature.shared.auth.ui.TermOfUseScreen
import com.livon.app.feature.shared.auth.ui.NicknameScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.livon.app.feature.shared.auth.ui.HealthInfoConditionScreen
import com.livon.app.feature.shared.auth.ui.LifestyleSmokingScreen
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val Landing = "landing"
    const val Terms = "terms_of_use"
    const val RoleSelect = "role_select"
    const val EmailLogin = "email_login"
    const val EmailSetup = "email_setup"
    const val EmailVerify = "email_verify"
    const val PasswordSetup = "password_setup"
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
            onComplete = { mode ->
                Log.d("AppNavGraph","RoleSelect -> EmailSetup (role=$mode)")
                // pass role as query param
                navController.navigate("${Routes.EmailSetup}?role=${URLEncoder.encode(mode, "UTF-8")}")
            }
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

    // EmailSetup accepts optional role
    composable(route = "${Routes.EmailSetup}?role={role}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        EmailSetupScreen(
            modifier = androidx.compose.ui.Modifier,
            onBack = { navController.popBackStack() },
            onNext = { email -> Log.d("AppNavGraph","EmailSetup -> EmailVerify (email=$email,role=$role)"); navController.navigate("${Routes.EmailVerify}?role=${URLEncoder.encode(role, "UTF-8")}") }
        )
    }

    // EmailVerify keeps role
    composable(route = "${Routes.EmailVerify}?role={role}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        EmailVerifyScreen(
            modifier = androidx.compose.ui.Modifier,
            onBack = { navController.popBackStack() },
            onVerified = {
                Log.d("AppNavGraph","EmailVerify -> PasswordSetup (role=$role)")
                navController.navigate("${Routes.PasswordSetup}?role=${URLEncoder.encode(role, "UTF-8")}")
            }
        )
    }

    // PasswordSetup -> NickName
    composable(route = "${Routes.PasswordSetup}?role={role}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        PasswordSetupScreen(
            modifier = androidx.compose.ui.Modifier,
            onBack = { navController.popBackStack() },
            onNext = { password ->
                Log.d("AppNavGraph","PasswordSetup -> NickName (role=$role)")
                navController.navigate("${Routes.NickName}?role=${URLEncoder.encode(role, "UTF-8")}")
            }
        )
    }

    // NickName: navigate from password to nickname, maintain role
    composable(route = "${Routes.NickName}?role={role}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        NicknameScreen(
            onBack = { navController.popBackStack() },
            onNext = { nickname ->
                val enc = URLEncoder.encode(nickname, "UTF-8")
                Log.d("AppNavGraph","NickName -> MemberTypeSelect (nickname=$nickname, role=$role)")
                navController.navigate("${Routes.MemberTypeSelect}?role=${URLEncoder.encode(role, "UTF-8")}&nickname=$enc")
            }
        )
    }

    // MemberTypeSelect accepts role & nickname
    composable(route = "${Routes.MemberTypeSelect}?role={role}&nickname={nickname}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" }, navArgument("nickname") { type = NavType.StringType; defaultValue = "" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
        MemberTypeSelectScreen(
            onBack = { navController.popBackStack() },
            onComplete = { mode -> Log.d("AppNavGraph","MemberTypeSelect -> next (mode=$mode, role=$role, nickname=$nickname)"); if (mode == "business") navController.navigate("${Routes.CompanySelect}?role=${URLEncoder.encode(role, "UTF-8")}&nickname=${URLEncoder.encode(nickname, "UTF-8")}") else navController.navigate("${Routes.GenderSelect}?role=${URLEncoder.encode(role, "UTF-8")}&nickname=${URLEncoder.encode(nickname, "UTF-8")}") }
        )
    }

    // CompanySelect keeps role & nickname
    composable(route = "${Routes.CompanySelect}?role={role}&nickname={nickname}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" }, navArgument("nickname") { type = NavType.StringType; defaultValue = "" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
        CompanySelectScreen(
            onBack = { navController.popBackStack() },
            onNext = { navController.navigate("${Routes.GenderSelect}?role=${URLEncoder.encode(role, "UTF-8")}&nickname=${URLEncoder.encode(nickname, "UTF-8")}") }
        )
    }

    // GenderSelect keeps role & nickname
    composable(route = "${Routes.GenderSelect}?role={role}&nickname={nickname}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" }, navArgument("nickname") { type = NavType.StringType; defaultValue = "" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
        GenderSelectScreen(
            onBack = { navController.popBackStack() },
            onNext = { gender -> Log.d("AppNavGraph","GenderSelect -> Birthday (gender=$gender, role=$role, nickname=$nickname)"); navController.navigate("${Routes.Birthday}?role=${URLEncoder.encode(role, "UTF-8")}&nickname=${URLEncoder.encode(nickname, "UTF-8")}") }
        )
    }

    // Birthday keeps role & nickname
    composable(route = "${Routes.Birthday}?role={role}&nickname={nickname}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" }, navArgument("nickname") { type = NavType.StringType; defaultValue = "" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
        BirthdayScreen(
            onBack = { navController.popBackStack() },
            onNext = { year, month, day -> Log.d("AppNavGraph","Birthday -> ProfilePhoto (y=$year,m=$month,d=$day, role=$role, nickname=$nickname)"); navController.navigate("${Routes.ProfilePhoto}?role=${URLEncoder.encode(role, "UTF-8")}&nickname=${URLEncoder.encode(nickname, "UTF-8")}") }
        )
    }

    // ProfilePhoto keeps role & nickname; onComplete -> signup_complete with username & role
    composable(route = "${Routes.ProfilePhoto}?role={role}&nickname={nickname}", arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "member" }, navArgument("nickname") { type = NavType.StringType; defaultValue = "" })) { backStackEntry ->
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
        ProfilePhotoSelectScreen(onBack = { navController.popBackStack() }, onComplete = {
            val enc = URLEncoder.encode(nickname, "UTF-8")
            Log.d("AppNavGraph","ProfilePhoto -> signup_complete (nickname=$nickname, role=$role)")
            navController.navigate("signup_complete?username=$enc&role=${URLEncoder.encode(role, "UTF-8")}")
        })
    }

    // SignUpComplete: accept username and role; decide next navigation based on role
    composable(
        route = "signup_complete?username={username}&role={role}",
        arguments = listOf(navArgument("username") { type = NavType.StringType; defaultValue = "" }, navArgument("role") { type = NavType.StringType; defaultValue = "member" })
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("username") ?: ""
        val username = try { URLDecoder.decode(encoded, "UTF-8") } catch (t: Throwable) { encoded }
        val role = backStackEntry.arguments?.getString("role") ?: "member"
        SignUpCompleteScreen(username = if (username.isBlank()) "회원" else username, onStart = {
            Log.d("AppNavGraph","SignUpComplete onStart: role=$role")
            if (role == "member") {
                // member: go through health info sequence
                navController.navigate(Routes.HealthHeight)
            } else {
                // coach: go to home
                navController.navigate(Routes.MemberHome)
            }
        })
    }

    // Health flow
    composable(Routes.HealthHeight) {
        HealthInfoHeightScreen(
            onBack = { navController.popBackStack() },
            onNext = { height ->
                Log.d("AppNavGraph", "HealthHeight -> HealthWeight (height=$height)")
                navController.navigate(Routes.HealthWeight)
            }
        )
    }
    composable(Routes.HealthWeight) {
        HealthInfoWeightScreen(
            onBack = { navController.popBackStack() },
            onNext = { weight ->
                Log.d("AppNavGraph", "HealthWeight -> HealthSurvey (weight=$weight)")
                navController.navigate(Routes.HealthSurvey)
            }
        )
    }
    // Start of health survey sequence
    composable(Routes.HealthSurvey) {
        // Use first survey screen (condition) as entry point; it will navigate to lifestyle when done
        HealthInfoConditionScreen(
            onBack = { navController.popBackStack() },
            onNext = { selected ->
                Log.d("AppNavGraph", "HealthSurvey(condition) -> LifestyleSurvey (selected=$selected)")
                navController.navigate(Routes.LifeStyleSurvey)
            }
        )
    }
    composable(Routes.LifeStyleSurvey) {
        LifestyleSmokingScreen(
            onBack = { navController.popBackStack() },
            onNext = { selected ->
                Log.d("AppNavGraph", "LifestyleSurvey(smoking) -> MemberHome (selected=$selected)")
                navController.navigate(Routes.MemberHome)
            }
        )
    }
}
