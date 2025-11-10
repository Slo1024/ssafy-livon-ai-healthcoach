// com/livon/app/navigation/MemberNavGraph.kt
package com.livon.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.livon.app.R
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.livon.app.feature.member.home.ui.MemberHomeRoute
import com.livon.app.feature.member.reservation.ui.*
import com.livon.app.feature.member.my.MyInfoUiState
import com.livon.app.feature.member.my.MyInfoScreen
import com.livon.app.feature.member.my.MyPageScreen
import com.livon.app.feature.shared.auth.ui.ReservationModeSelectScreen
import com.livon.app.feature.shared.auth.ui.SignupState
import com.livon.app.feature.member.home.ui.DataMetric
import java.net.URLDecoder
import java.time.LocalDate
import com.livon.app.feature.member.reservation.vm.ClassReservationViewModel

// Added imports required for composable UI rendering in this file
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf

fun isDebugBuild(): Boolean {
    return try {
        val cls = Class.forName("com.livon.app.BuildConfig")
        val f = cls.getField("DEBUG")
        f.getBoolean(null)
    } catch (t: Throwable) {
        // If reflection fails, assume dev environment true to aid development.
        true
    }
}

fun NavGraphBuilder.memberNavGraph(nav: NavHostController) {

    // No dev-mock data retained here: application uses only server API responses.

    composable(Routes.MemberHome) {
        // Build metrics from SignupState if available, otherwise show defaults
        fun withUnit(value: String?, unit: String): String = value?.let {
            val t = it.trim()
            if (t.isEmpty()) "-" else if (t.endsWith(unit)) t else "$t$unit"
        } ?: "-"

        // setup UserViewModel to fetch current user's info
        val userApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.UserApiService::class.java)
        val userRepo = remember { com.livon.app.domain.repository.UserRepository(userApi) }
        val userVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.home.vm.UserViewModel(userRepo) as T
            }
        }) as com.livon.app.feature.member.home.vm.UserViewModel

        val userState by userVm.uiState.collectAsState()
        LaunchedEffect(Unit) { userVm.load() }

        // cache the info locally to allow smart casts and avoid repeated complex access
        val info = userState.info

        val metrics = if (info != null) listOf(
            DataMetric("키", info.heightCm ?: "-", "평균: 169cm"),
            DataMetric("몸무게", info.weightKg ?: "-", "평균: 60kg"),
            DataMetric("기저질환", info.condition ?: "-", "-"),
            DataMetric("수면 상태", info.sleepQuality ?: "-", "-"),
            DataMetric("복약 여부", info.medication ?: "-", "-"),
            DataMetric("통증 부위", info.painArea ?: "-", "-"),
            DataMetric("스트레스", info.stress ?: "-", "-"),
            DataMetric("흡연 여부", info.smoking ?: "-", "-"),
            DataMetric("음주", info.alcohol ?: "-", "-"),
            DataMetric("수면 시간", info.sleepHours ?: "-", "평균: 7시간"),
            DataMetric("활동 수준", info.activityLevel ?: "-", "-"),
            DataMetric("카페인", info.caffeine ?: "-", "-")
        ) else listOf(
            DataMetric("키", withUnit(SignupState.heightCm, "cm"), "평균: 169cm"),
            DataMetric("몸무게", withUnit(SignupState.weightKg, "kg"), "평균: 60kg"),
            DataMetric("기저질환", SignupState.condition ?: "-", "-"),
            DataMetric("수면 상태", SignupState.sleepQuality ?: "-", "-"),
            DataMetric("복약 여부", SignupState.medication ?: "-", "-"),
            DataMetric("통증 부위", SignupState.painArea ?: "-", "-"),
            DataMetric("스트레스", SignupState.stress ?: "-", "-"),
            DataMetric("흡연 여부", SignupState.smoking ?: "-", "-"),
            DataMetric("음주", SignupState.alcohol ?: "-", "-"),
            DataMetric("수면 시간", SignupState.sleepHours?.let { if (it.endsWith("시간")) it else "${it}시간" } ?: "-", "평균: 7시간"),
            DataMetric("활동 수준", SignupState.activityLevel ?: "-", "-"),
            DataMetric("카페인", SignupState.caffeine ?: "-", "-")
        )

        // setup Reservation ViewModel (no DI)
        // ReservationRepository is an interface; use concrete implementation from data layer
        // ReservationRepositoryImpl internally creates its Retrofit service, so we can instantiate it directly.
        val reservationRepo = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepo) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val resState by reservationVm.uiState.collectAsState()
        LaunchedEffect(Unit) { reservationVm.loadUpcoming() }

        MemberHomeRoute(
            onTapBooking = { nav.navigate("reservation_model_select") },
            onTapReservations = { nav.navigate("reservations") },
            onTapMyPage = { nav.navigate("mypage") },
            metrics = metrics,
            upcoming = emptyList(),
            upcomingReservations = if (resState.items.isNotEmpty()) resState.items else if (isDebugBuild()) listOf() else emptyList(),
            companyName = null,
            nickname = userState.info?.nickname,
            modifier = Modifier
        )
    }

    // MyPage route: shows user's profile/settings page
    composable("mypage") {
        MyPageScreen(
            onBack = { nav.popBackStack() },
            onClickHealthInfo = { nav.navigate("myinfo") },
            onClickFaq = { /* TODO: navigate to FAQ */ }
        )
    }

    // MyInfo route: personal health info screen
    composable("myinfo") {
        // Safe conditional branch for MyInfo feature
        // Try to read a compile-time feature flag BuildConfig.FEATURE_MYINFO via reflection.
        // If it's not present, default to enabling in debug builds and disabling in release.
        val featureMyInfoEnabled: Boolean = try {
            val cls = Class.forName("com.livon.app.BuildConfig")
            val f = cls.getField("FEATURE_MYINFO")
            f.getBoolean(null)
        } catch (t: Throwable) {
            // No explicit flag: enable on debug builds to aid development, disable otherwise
            isDebugBuild()
        }

        if (featureMyInfoEnabled) {
            // Provide a minimal default state here; in real app this should come from ViewModel or nav arguments
            val defaultState = MyInfoUiState(
                nickname = "",
                gender = null,
                birthday = null,
                profileImageUri = null,
                heightCm = null,
                weightKg = null,
                condition = null,
                sleepQuality = null,
                medication = null,
                painArea = null,
                stress = null,
                smoking = null,
                alcohol = null,
                sleepHours = null,
                activityLevel = null,
                caffeine = null
            )
            MyInfoScreen(state = defaultState, onBack = { nav.popBackStack() })
        } else {
            // Fallback UI when feature disabled: redirect back to MyPage (or show a disabled screen)
            MyPageScreen(onBack = { nav.popBackStack() })
        }
    }





    composable("reservation_model_select") {
        ReservationModeSelectScreen(
            onComplete = { mode ->
                if (mode == "personal") nav.navigate("coach_list") else nav.navigate("class_reservation")
            },
            onBack = { nav.popBackStack() }
        )
    }

    composable("coach_list") {
        // network-backed coach list
        val coachApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.CoachApiService::class.java)
        val coachRepo = remember { com.livon.app.domain.repository.CoachRepository(coachApi) }
        val coachVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.CoachListViewModel(coachRepo) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.CoachListViewModel

        val coachState by coachVm.uiState.collectAsState()
        LaunchedEffect(Unit) { coachVm.load() }

        val coachesToShow = if (coachState.coaches.isNotEmpty()) coachState.coaches else emptyList()
        val isCorporate = false
        val loadMore = false

        CoachListScreen(
            coaches = coachesToShow,
            onBack = { nav.popBackStack() },
            modifier = Modifier,
            isCorporateUser = isCorporate,
            showLoadMore = loadMore,
            onLoadMore = {},
            // navigate with coach id so detail can receive which coach to show
            onCoachClick = { coach -> nav.navigate("coach_detail/${coach.id}/personal") }
        )
    }

    // coach_detail accepts coachId and mode (personal/group)
    composable("coach_detail/{coachId}/{mode}") { backStackEntry ->
        val coachId = backStackEntry.arguments?.getString("coachId")
        val mode = backStackEntry.arguments?.getString("mode") ?: "personal"
        val showSchedule = mode == "personal"

        // Provide a ViewModel factory so CoachDetailScreen can load remote detail
        val coachApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.CoachApiService::class.java)
        val coachRepo = remember { com.livon.app.domain.repository.CoachRepository(coachApi) }
        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.CoachDetailViewModel(coachRepo) as T
            }
        }

        CoachDetailScreen(
            coachId = coachId ?: "",
            onBack = { nav.popBackStack() },
            showSchedule = showSchedule,
            viewModelFactory = factory,
            onReserve = { coachName, date, time ->
                // encode coachId, coachName, date and time into route so QnA screen can perform reservation
                val encodedName = java.net.URLEncoder.encode(coachName, "UTF-8")
                val iso = date.toString() // yyyy-MM-dd
                val timeEnc = java.net.URLEncoder.encode(time, "UTF-8")
                nav.navigate("qna_submit/${coachId}/${encodedName}/${iso}/${timeEnc}")
            }
        )
    }

    // qna_submit now accepts coachId and time so we can call reservation API from here
    composable("qna_submit/{coachId}/{coachName}/{date}/{time}") { backStackEntry ->
        val encodedName = backStackEntry.arguments?.getString("coachName") ?: ""
        val decodedName = try { URLDecoder.decode(encodedName, "UTF-8") } catch (t: Throwable) { encodedName }
        val coachIdArg = backStackEntry.arguments?.getString("coachId") ?: ""
        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
        val parsedDate = try { LocalDate.parse(dateStr) } catch (t: Throwable) { LocalDate.now() }
        val timeRaw = backStackEntry.arguments?.getString("time") ?: ""
        val decodedTime = try { URLDecoder.decode(timeRaw, "UTF-8") } catch (t: Throwable) { timeRaw }

        // helper: convert incoming time token (like "AM_9:00" / "PM_1:00" or "09:00" / "9:00") to hour in 24h
        fun timeTokenToHour(tok: String): Int {
            return try {
                val s = tok.trim()
                when {
                    s.startsWith("AM_") || s.startsWith("am_") -> {
                        val hh = s.substringAfter("_").split(":")[0].toIntOrNull() ?: 9
                        // 12 AM -> 0 hour, others as-is
                        if (hh % 12 == 0) 0 else (hh % 12)
                    }
                    s.startsWith("PM_") || s.startsWith("pm_") -> {
                        val hh = s.substringAfter("_").split(":")[0].toIntOrNull() ?: 1
                        // 12 PM -> 12, others add 12
                        if (hh % 12 == 0) 12 else (hh % 12) + 12
                    }
                    else -> {
                        // plain form like "09:00" or "9:00" -> parse hour directly
                        val hh = s.split(":")[0].toIntOrNull() ?: 9
                        hh % 24
                    }
                }
            } catch (t: Throwable) {
                9 // fallback to 9am
            }
        }

        // Use ReservationViewModel to perform reservation and observe result
        val reservationRepoForQna = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVmForQna = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoForQna) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val actionState by reservationVmForQna.actionState.collectAsState()

        // When reservation action completes, navigate to reservations (success or failure)
        LaunchedEffect(actionState.success) {
            if (actionState.success != null) {
                nav.navigate("reservations") {
                    popUpTo(Routes.MemberHome) { inclusive = false }
                }
            }
        }

        QnASubmitScreen(
            coachName = decodedName,
            selectedDate = parsedDate,
            onBack = { nav.popBackStack() },
            onConfirmReservation = { questions ->
                val preQnA = questions.joinToString("\n")
                val hour = timeTokenToHour(decodedTime)
                val startAt = java.time.LocalDateTime.of(parsedDate, java.time.LocalTime.of(hour,0))
                val endAt = startAt.plusHours(1)

                // delegate to ViewModel
                reservationVmForQna.reserveCoach(coachIdArg, startAt, endAt, preQnA)
            },
            onNavigateHome = { nav.navigate(Routes.MemberHome) },
            onNavigateToMyHealthInfo = { /* noop */ },
            navController = nav,
            externalError = actionState.errorMessage
        )
    }

    // --- ADDED FALLBACK ROUTE: accept missing 'time' segment and default to empty time ---
    composable("qna_submit/{coachId}/{coachName}/{date}") { backStackEntry ->
        // reuse the same logic but supply an empty time token so ViewModel can handle it
        val encodedName = backStackEntry.arguments?.getString("coachName") ?: ""
        val decodedName = try { URLDecoder.decode(encodedName, "UTF-8") } catch (t: Throwable) { encodedName }
        val coachIdArg = backStackEntry.arguments?.getString("coachId") ?: ""
        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
        val parsedDate = try { LocalDate.parse(dateStr) } catch (t: Throwable) { LocalDate.now() }
        val decodedTime = "" // no time provided by caller

        fun timeTokenToHour(tok: String): Int {
            return try {
                val s = tok.trim()
                when {
                    s.startsWith("AM_") || s.startsWith("am_") -> {
                        val hh = s.substringAfter("_").split(":")[0].toIntOrNull() ?: 9
                        if (hh % 12 == 0) 0 else (hh % 12)
                    }
                    s.startsWith("PM_") || s.startsWith("pm_") -> {
                        val hh = s.substringAfter("_").split(":")[0].toIntOrNull() ?: 1
                        if (hh % 12 == 0) 12 else (hh % 12) + 12
                    }
                    else -> {
                        val hh = s.split(":")[0].toIntOrNull() ?: 9
                        hh % 24
                    }
                }
            } catch (t: Throwable) {
                9
            }
        }

        // ViewModel setup
        val reservationRepoForQna = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVmForQna = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoForQna) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val actionState by reservationVmForQna.actionState.collectAsState()

        LaunchedEffect(actionState.success) {
            if (actionState.success != null) {
                nav.navigate("reservations") {
                    popUpTo(Routes.MemberHome) { inclusive = false }
                }
            }
        }

        QnASubmitScreen(
            coachName = decodedName,
            selectedDate = parsedDate,
            onBack = { nav.popBackStack() },
            onConfirmReservation = { questions ->
                val preQnA = questions.joinToString("\n")
                val hour = timeTokenToHour(decodedTime)
                val startAt = java.time.LocalDateTime.of(parsedDate, java.time.LocalTime.of(hour,0))
                val endAt = startAt.plusHours(1)

                // delegate to ViewModel
                reservationVmForQna.reserveCoach(coachIdArg, startAt, endAt, preQnA)
            },
            onNavigateHome = { nav.navigate(Routes.MemberHome) },
            onNavigateToMyHealthInfo = { /* noop */ },
            navController = nav,
            externalError = actionState.errorMessage
        )
    }

    // qna_submit fallback (no args) kept for previews/tests
    composable("qna_submit") {
        QnASubmitScreen(
            coachName = "코치",
            selectedDate = LocalDate.now(),
            onBack = { nav.popBackStack() },
            onConfirmReservation = { _ -> nav.navigate("reservations") },
            onNavigateHome = { nav.navigate(Routes.MemberHome) },
            onNavigateToMyHealthInfo = { /* noop */ },
            navController = nav
        )
    }

    composable("class_reservation") {
        // Use network-backed group consultation list when available
        val groupApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.GroupConsultationApiService::class.java)
        val groupRepo = remember { com.livon.app.domain.repository.GroupConsultationRepository(groupApi) }

        // Create a simple ViewModel on the fly to fetch classes
        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ClassReservationViewModel(groupRepo) as T
            }
        }

        val vm = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory) as ClassReservationViewModel
        val vmState by vm.uiState.collectAsState()
        LaunchedEffect(Unit) { vm.loadClasses() }

        val classesToShow = if (vmState.items.isNotEmpty()) vmState.items else emptyList()

        ClassReservationScreen(
            classes = classesToShow,
            onCardClick = { item -> nav.navigate("class_detail/${item.id}") },
            onCoachClick = { coachId -> nav.navigate("coach_detail/$coachId/group") },
            navController = nav
        )
    }

    // class_detail/{classId} route: show ClassDetailScreen for the selected class id
    composable("class_detail/{classId}") { backStackEntry ->
        val classId = backStackEntry.arguments?.getString("classId") ?: ""

        // Create repo to fetch class detail (network-backed, with dev fallback inside repo)
        val groupApi = com.livon.app.core.network.RetrofitProvider.createService(
            com.livon.app.data.remote.api.GroupConsultationApiService::class.java
        )
        val groupRepo = remember { com.livon.app.domain.repository.GroupConsultationRepository(groupApi) }

        // Local mutable state to hold loaded class detail
        val detailState = remember { mutableStateOf<SampleClassInfo?>(null) }
        val errorState = remember { mutableStateOf<String?>(null) }
        val loadingState = remember { mutableStateOf(true) }

        // Load detail when entering this composable
        LaunchedEffect(classId) {
            loadingState.value = true
            errorState.value = null
            try {
                val res = groupRepo.fetchClassDetail(classId)
                if (res.isSuccess) {
                    detailState.value = res.getOrNull()
                } else {
                    errorState.value = res.exceptionOrNull()?.message ?: "Unknown error"
                }
            } catch (t: Throwable) {
                errorState.value = t.message
            } finally {
                loadingState.value = false
            }
        }

        // UI: show loading indicator until detail is available
        if (loadingState.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val item = detailState.value
            if (item != null) {
                ClassDetailScreen(
                    className = item.className,
                    coachName = item.coachName,
                    classInfo = item.description,
                    onBack = { nav.popBackStack() },
                    onReserveClick = { nav.navigate("qna_submit_class/${item.id}") },
                    onNavigateHome = { nav.navigate(Routes.MemberHome) },
                    onNavigateToMyPage = { nav.navigate("mypage") },
                    imageResId = R.drawable.ic_classphoto,
                    navController = nav
                )
            } else {
                // show error / fallback UI
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = errorState.value ?: "클래스 정보를 불러올 수 없습니다.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { nav.popBackStack() }) {
                            Text(text = "뒤로가기")
                        }
                    }
                }
            }
        }
    }

    // Reservations (예약 현황) screen
    composable("reservations") {
        // Try to use ReservationViewModel to fetch real items; fallback to DevReservationStore on dev
        // Use concrete implementation instead of interface here as well
        val reservationRepo = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepo) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val resState by reservationVm.uiState.collectAsState()
        LaunchedEffect(Unit) { reservationVm.loadUpcoming() }

        // If API returned items, use them. Otherwise in dev return our in-memory store
        val currentList = if (resState.items.isNotEmpty()) resState.items else emptyList()

        ReservationStatusScreen(
            current = currentList,
            past = emptyList(),
            onBack = {
                // Ensure back always goes to home, not back through the reservation flow
                val homeRoute = Routes.MemberHome
                // If we're already on home, just pop; otherwise navigate and clear stack up to home
                try {
                    if (nav.currentDestination?.route == homeRoute) {
                        nav.popBackStack()
                    } else {
                        nav.navigate(homeRoute) {
                            popUpTo(homeRoute) { inclusive = false }
                        }
                    }
                } catch (t: Throwable) {
                    // fallback: conservative pop
                    nav.popBackStack()
                }
            },
            onDetail = { /* TODO: navigate to reservation detail */ },
            onCancel = { /* TODO: cancel reservation via API */ },
            onJoin = { /* TODO: join live session */ },
            onAiAnalyze = { /* TODO: show AI analysis */ }
        )
    }
    // class QnA/reserve route: class reservations (no time selection)
    composable("qna_submit_class/{classId}") { backStackEntry ->
        val classId = backStackEntry.arguments?.getString("classId") ?: ""

        // setup reservation vm
        val reservationRepoForClass = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVmForClass = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoForClass) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val actionState by reservationVmForClass.actionState.collectAsState()

        // navigate back to reservations on success
        LaunchedEffect(actionState.success) {
            if (actionState.success == true) {
                nav.navigate("reservations") {
                    popUpTo(Routes.MemberHome) { inclusive = false }
                }
            }
        }

        // Show QnA screen but on confirm call reserveClass
        QnASubmitScreen(
            coachName = "클래스",
            selectedDate = java.time.LocalDate.now(),
            onBack = { nav.popBackStack() },
            onConfirmReservation = { _ ->
                // ignore QnA text for class reservation - backend only needs classId in path
                reservationVmForClass.reserveClass(classId)
            },
            onNavigateHome = { nav.navigate(Routes.MemberHome) },
            onNavigateToMyHealthInfo = { /* noop */ },
            navController = nav,
            externalError = actionState.errorMessage
        )
    }
}
