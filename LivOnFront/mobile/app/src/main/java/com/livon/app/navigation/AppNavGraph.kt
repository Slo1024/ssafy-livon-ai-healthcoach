// kotlin
package com.livon.app.navigation

import android.util.Log
import androidx.compose.runtime.*
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
import com.livon.app.feature.shared.auth.ui.SignupState
import com.livon.app.feature.shared.auth.ui.HealthInfoMedicationScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoPainDiscomfortScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoSleepQualityScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoStressScreen
import com.livon.app.feature.shared.auth.ui.LifestyleActivityLevelScreen
import com.livon.app.feature.shared.auth.ui.LifestyleAlcoholIntakeScreen
import com.livon.app.feature.shared.auth.ui.LifestyleCaffeinIntakeScreen
import com.livon.app.feature.shared.auth.ui.LifestyleSleepDurationScreen
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

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
                SignupState.role = mode
                // pass role as query param
                navController.navigate("${Routes.EmailSetup}?role=${URLEncoder.encode(mode, "UTF-8")}")
            }
        )
    }

    composable(Routes.EmailLogin) {
        // create api/repo/vm for login flow (no DI)
        val authApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.AuthApiService::class.java)
        val authRepo = remember { com.livon.app.domain.repository.AuthRepository(authApi) }
        val factory = remember {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return com.livon.app.feature.member.auth.vm.AuthViewModel(authRepo) as T
                }
            }
        }
        val authVm: com.livon.app.feature.member.auth.vm.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

        // observe login state and navigate on success
        val authState by authVm.state.collectAsState()
        LaunchedEffect(authState.success) {
            if (authState.success) {
                // Navigate to member_home and clear backstack up to landing
                navController.navigate(Routes.MemberHome) {
                    popUpTo(Routes.Landing) { inclusive = true }
                }
            }
        }

        EmailLoginScreen(
            onBack = { navController.popBackStack() },
            onLogin = { email, pw -> authVm.login(email, pw) },
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
                SignupState.passwordSet = true
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
                SignupState.nickname = nickname
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
                SignupState.heightCm = height
                navController.navigate(Routes.HealthWeight)
            }
        )
    }
    composable(Routes.HealthWeight) {
        HealthInfoWeightScreen(
            onBack = { navController.popBackStack() },
            onNext = { weight ->
                Log.d("AppNavGraph", "HealthWeight -> HealthSurvey (weight=$weight)")
                SignupState.weightKg = weight
                navController.navigate(Routes.HealthSurvey)
            }
        )
    }
    // Start of health survey sequence
    composable(Routes.HealthSurvey) {
        // Use first survey screen (condition) as entry point; chain the rest here
        HealthInfoConditionScreen(
            onBack = { navController.popBackStack() },
            onNext = { condition ->
                SignupState.condition = condition
                Log.d("AppNavGraph", "HealthSurvey(condition) -> sleep (condition=$condition)")
                // push next: sleep quality
                navController.navigate("health_survey_sleep")
            }
        )
    }
    composable("health_survey_sleep") {
        HealthInfoSleepQualityScreen(onBack = { navController.popBackStack() }, onNext = { sleep ->
            SignupState.sleepQuality = sleep
            Log.d("AppNavGraph","HealthSurvey(sleep) -> medication (sleep=$sleep)")
            navController.navigate("health_survey_medication")
        })
    }
    composable("health_survey_medication") {
        HealthInfoMedicationScreen(onBack = { navController.popBackStack() }, onNext = { med ->
            SignupState.medication = med
            Log.d("AppNavGraph","HealthSurvey(med) -> pain (med=$med)")
            navController.navigate("health_survey_pain")
        })
    }
    composable("health_survey_pain") {
        HealthInfoPainDiscomfortScreen(onBack = { navController.popBackStack() }, onNext = { pain ->
            SignupState.painArea = pain
            Log.d("AppNavGraph","HealthSurvey(pain) -> stress (pain=$pain)")
            navController.navigate("health_survey_stress")
        })
    }
    composable("health_survey_stress") {
        HealthInfoStressScreen(onBack = { navController.popBackStack() }, onNext = { stress ->
            SignupState.stress = stress
            Log.d("AppNavGraph","HealthSurvey(stress) -> lifestyle (stress=$stress)")
            navController.navigate(Routes.LifeStyleSurvey)
        })
    }

    // Lifestyle flow chain
    composable(Routes.LifeStyleSurvey) {
        LifestyleSmokingScreen(onBack = { navController.popBackStack() }, onNext = { smoking ->
            SignupState.smoking = smoking
            Log.d("AppNavGraph","Lifestyle(smoking) -> alcohol (smoking=$smoking)")
            navController.navigate("lifestyle_alcohol")
        })
    }
    composable("lifestyle_alcohol") {
        LifestyleAlcoholIntakeScreen(onBack = { navController.popBackStack() }, onNext = { alcohol ->
            SignupState.alcohol = alcohol
            Log.d("AppNavGraph","Lifestyle(alcohol) -> sleepDuration (alcohol=$alcohol)")
            navController.navigate("lifestyle_sleep")
        })
    }
    composable("lifestyle_sleep") {
        LifestyleSleepDurationScreen(onBack = { navController.popBackStack() }, onNext = { hours ->
            SignupState.sleepHours = hours
            Log.d("AppNavGraph","Lifestyle(sleep) -> activity (hours=$hours)")
            navController.navigate("lifestyle_activity")
        })
    }
    composable("lifestyle_activity") {
        LifestyleActivityLevelScreen(onBack = { navController.popBackStack() }, onNext = { activity ->
            SignupState.activityLevel = activity
            Log.d("AppNavGraph","Lifestyle(activity) -> caffeine (activity=$activity)")
            navController.navigate("lifestyle_caffeine")
        })
    }
    composable("lifestyle_caffeine") {
        val coroutineScope = rememberCoroutineScope()
        LifestyleCaffeinIntakeScreen(onBack = { navController.popBackStack() }, onNext = { caffeine ->
            SignupState.caffeine = caffeine
            Log.d("AppNavGraph","Lifestyle(caffeine) finished (caffeine=$caffeine)")
            // Build request from SignupState and post to backend, then navigate home on success
            val userApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.UserApiService::class.java)
            val userRepo = com.livon.app.domain.repository.UserRepository(userApi)

            coroutineScope.launch {
                try {
                    val req = com.livon.app.data.remote.api.HealthSurveyRequest(
                        steps = 0,
                        sleepTime = SignupState.sleepHours?.toIntOrNull() ?: 0,
                        disease = SignupState.condition,
                        sleepQuality = SignupState.sleepQuality,
                        medicationsInfo = SignupState.medication,
                        painArea = SignupState.painArea,
                        stressLevel = SignupState.stress,
                        smokingStatus = SignupState.smoking,
                        avgSleepHours = SignupState.sleepHours?.toIntOrNull() ?: 0,
                        activityLevel = SignupState.activityLevel,
                        caffeineIntakeLevel = SignupState.caffeine,
                        height = SignupState.heightCm?.toIntOrNull() ?: 0,
                        weight = SignupState.weightKg?.toIntOrNull() ?: 0
                    )

                    val res = userRepo.postHealthSurvey(req)
                    if (res.isSuccess) {
                        Log.d("AppNavGraph","Health survey posted successfully, returning to QnA submit")
                        // Try to return to a qna_submit route by popping the back stack safely.
                        try {
                            var safety = 0
                            var reached = false
                            // Pop back until we find a route that contains "qna_submit"
                            while (safety < 30) {
                                val currentRoute = navController.currentDestination?.route
                                if (currentRoute != null && currentRoute.contains("qna_submit")) {
                                    // mark the found entry so QnA screen can react
                                    navController.currentBackStackEntry?.savedStateHandle?.set("health_updated", true)
                                    reached = true
                                    break
                                }
                                // If we can't pop further, break
                                val popped = navController.popBackStack()
                                if (!popped) break
                                safety++
                            }

                            if (!reached) {
                                // fallback: if qna_submit wasn't found, navigate back conservatively to reservations
                                try {
                                    navController.navigate("reservations") { popUpTo(Routes.MemberHome) { inclusive = false } }
                                } catch (t: Throwable) {
                                    navController.popBackStack()
                                }
                            }
                        } catch (t: Throwable) {
                            Log.d("AppNavGraph", "Failed to pop back to QnA submit: ${t.message}")
                            navController.popBackStack()
                        }
                    } else {
                        Log.d("AppNavGraph","Health survey post failed: ${res.exceptionOrNull()?.message}")
                        // navigate back conservatively
                        navController.popBackStack()
                    }
                } catch (t: Throwable) {
                    Log.d("AppNavGraph","Health survey post exception: ${t.message}")
                    navController.popBackStack()
                }
            }
        })
    }
}
