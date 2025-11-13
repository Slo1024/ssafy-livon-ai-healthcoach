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
import com.livon.app.feature.shared.auth.ui.HealthInfoConditionScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoMedicationScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoPainDiscomfortScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoSleepQualityScreen
import com.livon.app.feature.shared.auth.ui.HealthInfoStressScreen
import com.livon.app.feature.shared.auth.ui.LifestyleActivityLevelScreen
import com.livon.app.feature.shared.auth.ui.LifestyleAlcoholIntakeScreen
import com.livon.app.feature.shared.auth.ui.LifestyleSleepDurationScreen
import com.livon.app.feature.shared.auth.ui.LifestyleCaffeinIntakeScreen
import com.livon.app.feature.shared.auth.ui.LifestyleSmokingScreen
import com.livon.app.feature.shared.auth.ui.SignupState
import java.net.URLEncoder
import java.time.LocalDate

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
    const val SignUpComplete = "signup_complete"
    // Named routes for health sub-steps
    const val HealthSleep = "health_survey_sleep"
    const val HealthMedication = "health_survey_medication"
    const val HealthPain = "health_survey_pain"
    const val HealthStress = "health_survey_stress"
    // Lifestyle sub-steps
    const val LifeAlcohol = "lifestyle_alcohol"
    const val LifeSleep = "lifestyle_sleep"
    const val LifeActivity = "lifestyle_activity"
    const val LifeCaffeine = "lifestyle_caffeine"
    const val ReservationModeSelect = "reservation_model_select"
    const val Reservations = "reservations"
    const val MyPage = "mypage"
    const val MemberHome = "member_home"
    const val MyInfo = "my_info"
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
    // Helper to avoid IllegalArgumentException when navigating to a non-registered route
    val safeNavigate: (String) -> Unit = { route ->
        try {
            if (navController.graph.findNode(route) != null) {
                navController.navigate(route)
            } else {
                Log.w("AppNavGraph", "Requested route not in graph: $route. Navigating to MemberHome instead.")
                navController.navigate(Routes.MemberHome)
            }
        } catch (t: Throwable) {
            Log.e("AppNavGraph", "safeNavigate failed for route=$route", t)
            try { navController.navigate(Routes.MemberHome) } catch (_: Throwable) {}
        }
    }

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
                try {
                    // give NavHost a moment to finish building graph children to avoid race
                    kotlinx.coroutines.delay(50)
                    if (navController.graph.findNode(Routes.MemberHome) != null) {
                        navController.navigate(Routes.MemberHome) {
                            // clear auth stack (landing & login) so back doesn't return to login
                            popUpTo(Routes.Landing) { inclusive = true }
                        }
                    } else {
                        // If destination still missing, log and attempt navigation anyway (best-effort)
                        Log.w("AppNavGraph", "MemberHome destination not found in nav graph; attempting navigate() anyway")
                        navController.navigate(Routes.MemberHome) {
                            popUpTo(Routes.Landing) { inclusive = true }
                        }
                    }
                } catch (t: Throwable) {
                    Log.e("AppNavGraph", "navigate to MemberHome failed", t)
                }
            }
        }

        EmailLoginScreen(
            onBack = { navController.popBackStack() },
            onLogin = { email, pw -> authVm.login(email, pw) },
            onSignUp = { navController.navigate(Routes.Terms) },
            onFindId = {},
            onFindPassword = {}
        )
    }

    composable(Routes.EmailSetup) {
        EmailSetupScreen(
            onBack = { navController.popBackStack() },
            onNext = { email ->
                Log.d("AppNavGraph","EmailSetup -> Nickname")
                navController.navigate(Routes.NickName)
            }
        )
    }

    composable(Routes.EmailVerify) {
        EmailVerifyScreen(
            onBack = { navController.popBackStack() },
            onVerified = {
                Log.d("AppNavGraph","EmailVerify -> PasswordSetup")
                navController.navigate(Routes.PasswordSetup)
            }
        )
    }

    composable(Routes.PasswordSetup) {
        PasswordSetupScreen(
            onBack = { navController.popBackStack() },
            onNext = { password ->
                Log.d("AppNavGraph","PasswordSetup -> MemberTypeSelect")
                navController.navigate(Routes.MemberTypeSelect)
            }
        )
    }

    composable(Routes.NickName) {
        NicknameScreen(
            onBack = { navController.popBackStack() },
            onNext = { nickname ->
                Log.d("AppNavGraph","Nickname -> CompanySelect (nickname=$nickname)")
                SignupState.nickname = nickname
                navController.navigate(Routes.CompanySelect)
            }
        )
    }

    composable(Routes.MemberTypeSelect) {
        MemberTypeSelectScreen(
            onBack = { navController.popBackStack() },
            onComplete = { memberType ->
                Log.d("AppNavGraph","MemberTypeSelect -> GenderSelect (memberType=$memberType)")
                SignupState.memberType = memberType
                navController.navigate(Routes.GenderSelect)
            }
        )
    }

    composable(Routes.CompanySelect) {
        CompanySelectScreen(
            onBack = { navController.popBackStack() },
            onNext = { company ->
                Log.d("AppNavGraph","CompanySelect -> Birthday (company=$company)")
                SignupState.company = company
                navController.navigate(Routes.Birthday)
            }
        )
    }

    composable(Routes.GenderSelect) {
        GenderSelectScreen(
            onBack = { navController.popBackStack() },
            onNext = { gender ->
                Log.d("AppNavGraph","GenderSelect -> HealthHeight (gender=$gender)")
                SignupState.gender = gender
                navController.navigate(Routes.HealthHeight)
            }
        )
    }

    composable(Routes.Birthday) {
        BirthdayScreen(
            onBack = { navController.popBackStack() },
            onNext = { year, month, day ->
                try {
                    val ld = LocalDate.of(year, month, day)
                    SignupState.birthday = ld
                } catch (_: Throwable) { /* ignore invalid date */ }
                Log.d("AppNavGraph","Birthday -> ProfilePhoto (y=$year m=$month d=$day)")
                navController.navigate(Routes.ProfilePhoto)
            }
        )
    }

    composable(Routes.ProfilePhoto) {
        ProfilePhotoSelectScreen(
            onBack = { navController.popBackStack() },
            onComplete = {
                Log.d("AppNavGraph","ProfilePhoto -> HealthWeight")
                // ProfilePhotoSelectScreen does not return uri; backend update happens elsewhere
                navController.navigate(Routes.HealthWeight)
            }
        )
    }

    composable(Routes.HealthHeight) {
        HealthInfoHeightScreen(
            onBack = { navController.popBackStack() },
            onNext = { height ->
                Log.d("AppNavGraph","HealthHeight -> HealthWeight (height=$height)")
                SignupState.height = height
                navController.navigate(Routes.HealthWeight)
            }
        )
    }

    composable(Routes.HealthWeight) {
        HealthInfoWeightScreen(
            onBack = { navController.popBackStack() },
            onNext = { weight ->
                Log.d("AppNavGraph","HealthWeight -> HealthSurvey (weight=$weight)")
                SignupState.weight = weight
                navController.navigate(Routes.HealthSurvey)
            }
        )
    }

    composable(Routes.HealthSurvey) {
        // HealthSurvey is split across multiple screens; start sequence with HealthInfoConditionScreen
        HealthInfoConditionScreen(
            onBack = { navController.popBackStack() },
            onNext = { condition ->
                SignupState.condition = condition
                Log.d("AppNavGraph", "HealthSurvey(condition) -> sleep (condition=$condition)")
                safeNavigate(Routes.HealthSleep)
            }
        )
    }

    // Health survey: 수면
    composable(Routes.HealthSleep) {
        HealthInfoSleepQualityScreen(
            onBack = { navController.popBackStack() },
            onNext = { sleep ->
                SignupState.sleepQuality = sleep
                Log.d("AppNavGraph", "HealthSurvey(sleep) -> medication (sleep=$sleep)")
                safeNavigate(Routes.HealthMedication)
            }
        )
    }

    // Health survey: 복약
    composable(Routes.HealthMedication) {
        HealthInfoMedicationScreen(
            onBack = { navController.popBackStack() },
            onNext = { med ->
                SignupState.medication = med
                Log.d("AppNavGraph", "HealthSurvey(med) -> pain (med=$med)")
                safeNavigate(Routes.HealthPain)
            }
        )
    }

    // Health survey: 통증/불편함
    composable(Routes.HealthPain) {
        HealthInfoPainDiscomfortScreen(
            onBack = { navController.popBackStack() },
            onNext = { pain ->
                SignupState.painArea = pain
                Log.d("AppNavGraph", "HealthSurvey(pain) -> stress (pain=$pain)")
                safeNavigate(Routes.HealthStress)
            }
        )
    }

    // Health survey: 스트레스
    composable(Routes.HealthStress) {
        HealthInfoStressScreen(
            onBack = { navController.popBackStack() },
            onNext = { stress ->
                SignupState.stress = stress
                Log.d("AppNavGraph", "HealthSurvey(stress) -> lifestyle (stress=$stress)")
                safeNavigate(Routes.LifeStyleSurvey)
            }
        )
    }

    // Lifestyle: entry point (shows smoking screen first)
    composable(Routes.LifeStyleSurvey) {
        LifestyleSmokingScreen(
            onBack = { navController.popBackStack() },
            onNext = { smoking ->
                SignupState.smoking = smoking
                Log.d("AppNavGraph","Lifestyle(smoking) -> alcohol (smoking=$smoking)")
                safeNavigate(Routes.LifeAlcohol)
            }
        )
    }

    // Lifestyle chain: alcohol
    composable(Routes.LifeAlcohol) {
        LifestyleAlcoholIntakeScreen(
            onBack = { navController.popBackStack() },
            onNext = { alcohol ->
                SignupState.alcohol = alcohol
                Log.d("AppNavGraph","Lifestyle(alcohol) -> sleepDuration (alcohol=$alcohol)")
                safeNavigate(Routes.LifeSleep)
            }
        )
    }

    // Lifestyle: sleep duration
    composable(Routes.LifeSleep) {
        LifestyleSleepDurationScreen(
            onBack = { navController.popBackStack() },
            onNext = { hours ->
                SignupState.sleepHours = hours
                Log.d("AppNavGraph","Lifestyle(sleep) -> activity (hours=$hours)")
                safeNavigate(Routes.LifeActivity)
            }
        )
    }

    // Lifestyle: activity level
    composable(Routes.LifeActivity) {
        LifestyleActivityLevelScreen(
            onBack = { navController.popBackStack() },
            onNext = { activity ->
                SignupState.activityLevel = activity
                Log.d("AppNavGraph","Lifestyle(activity) -> caffeine (activity=$activity)")
                safeNavigate(Routes.LifeCaffeine)
            }
        )
    }

    // Lifestyle: caffeine (final) — for now navigate back to MemberHome; backend posting is handled elsewhere
    composable(Routes.LifeCaffeine) {
        LifestyleCaffeinIntakeScreen(
            onBack = { navController.popBackStack() },
            onNext = { caffeine ->
                SignupState.caffeine = caffeine
                Log.d("AppNavGraph","Lifestyle(caffeine) finished (caffeine=$caffeine)")
                // Try to post survey to backend here (omitted). After success, check savedStateHandle to determine where to return.
                // The caller (QnA or MyInfo) saved an origin marker on its own backStackEntry before navigating here.
                // The immediate previousBackStackEntry should be that caller. Check it for qna_origin/myinfo_origin.
                try {
                    val prev = navController.previousBackStackEntry
                    val prevSaved = prev?.savedStateHandle

                    val qnaOrigin = prevSaved?.get<Map<String, String>>("qna_origin")
                    if (qnaOrigin != null) {
                        // clear origin and mark health_updated so QnASubmitScreen will re-open dialog
                        prevSaved.remove<Map<String, String>>("qna_origin")
                        prevSaved.set("health_updated", true)
                        try {
                            navController.popBackStack(prev.destination.id, false)
                            return@LifestyleCaffeinIntakeScreen
                        } catch (t: Throwable) {
                            Log.w("AppNavGraph", "Failed to popBackStack to QnA entry", t)
                        }
                    }

                    val myinfoOrigin = prevSaved?.get<Boolean>("myinfo_origin")
                    if (myinfoOrigin == true) {
                        prevSaved.remove<Boolean>("myinfo_origin")
                        prevSaved.set("health_updated", true)
                        safeNavigate(Routes.MyInfo)
                        return@LifestyleCaffeinIntakeScreen
                    }
                } catch (t: Throwable) {
                    Log.w("AppNavGraph", "Error while handling origin flags on previousBackStackEntry", t)
                }

                // Fallback: if previous entry wasn't found or didn't have qna_origin, but we have a marker route saved,
                // navigate to it and set health_updated on the newly-created entry so the QnASubmitScreen will show dialog.
                try {
                    val marker = SignupState.qnaMarkerRoute
                    if (!marker.isNullOrBlank()) {
                        SignupState.qnaMarkerRoute = null
                        navController.navigate(marker) {
                            // open as a new instance on top; we'll set savedStateHandle immediately after
                        }
                        // set flag so the new QnASubmitScreen re-opens dialog
                        navController.currentBackStackEntry?.savedStateHandle?.set("health_updated", true)
                        return@LifestyleCaffeinIntakeScreen
                    }
                } catch (t: Throwable) {
                    Log.w("AppNavGraph", "Failed to navigate to qnaMarkerRoute", t)
                }

                // Default fallback: go to MemberHome
                safeNavigate(Routes.MemberHome)
             }
         )
     }

    composable(Routes.SignUpComplete) {
        SignUpCompleteScreen(username = SignupState.nickname, onStart = {
            Log.d("AppNavGraph","SignUpComplete onStart called")
            navController.navigate(Routes.Landing) {
                popUpTo(Routes.Landing) { inclusive = true }
            }
        })
    }
}
