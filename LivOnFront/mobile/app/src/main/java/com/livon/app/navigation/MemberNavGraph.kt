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
import android.util.Log
import kotlinx.coroutines.awaitCancellation
import androidx.lifecycle.Observer
import com.livon.app.feature.member.reservation.ui.ReservationDetailType
import com.livon.app.feature.member.reservation.ui.CoachMini
import com.livon.app.feature.member.reservation.ui.SessionInfo

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

        // Observe health_updated flag set by AppNavGraph when health survey completes
        // Use the current backStackEntry as a LifecycleOwner to observe the savedStateHandle LiveData.
        // This avoids DisposableEffect/onDispose issues and registers an observer that is lifecycle-aware.
        val backEntry = nav.currentBackStackEntry
        LaunchedEffect(backEntry) {
            if (backEntry == null) return@LaunchedEffect
            val live = backEntry.savedStateHandle.getLiveData<Boolean>("health_updated")
            val observer = Observer<Boolean> { flag ->
                if (flag == true) {
                    try {
                        userVm.load()
                    } catch (t: Throwable) {
                        Log.w("MemberNavGraph", "Failed to reload user data", t)
                    }
                    try {
                        reservationVm.loadUpcoming()
                    } catch (t: Throwable) {
                        Log.w("MemberNavGraph", "Failed to reload reservation data", t)
                    }
                    backEntry.savedStateHandle.remove<Boolean>("health_updated")
                }
            }
            // Observe using the NavBackStackEntry as LifecycleOwner so it's removed automatically
            live.observe(backEntry, observer)
            try {
                awaitCancellation()
            } finally {
                live.removeObserver(observer)
            }
        }

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
            // ✅ 서버 연동 추가
            val userApi = com.livon.app.core.network.RetrofitProvider
                .createService(com.livon.app.data.remote.api.UserApiService::class.java)
            val userRepo = remember { com.livon.app.domain.repository.UserRepository(userApi) }
            val userVm = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return com.livon.app.feature.member.home.vm.UserViewModel(userRepo) as T
                    }
                }
            ) as com.livon.app.feature.member.home.vm.UserViewModel

            val userState by userVm.uiState.collectAsState()
            LaunchedEffect(Unit) { userVm.load() }

            val state = userState.info ?: MyInfoUiState(
                nickname = "",
                gender = null, birthday = null, profileImageUri = null,
                heightCm = null, weightKg = null, condition = null, sleepQuality = null,
                medication = null, painArea = null, stress = null, smoking = null,
                alcohol = null, sleepHours = null, activityLevel = null, caffeine = null
            )
            MyInfoScreen(state = state, onBack = { nav.popBackStack() })
        } else {
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
            if (actionState.success == true) {
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

                // Try to resolve coachId/startAt from savedStateHandle if nav-args were lost during health flow
                fun findSavedString(vararg keys: String): String? {
                    // check current backStackEntry (the composable's own savedStateHandle)
                    val ownSaved = backStackEntry.savedStateHandle
                    for (k in keys) {
                        val v = ownSaved.get<String>(k)
                        if (!v.isNullOrBlank()) return v
                    }
                    // check NavController current & previous entries' savedStateHandle for fallbacks
                    val currentSaved = nav.currentBackStackEntry?.savedStateHandle
                    if (currentSaved != null) {
                        for (k in keys) {
                            val v = currentSaved.get<String>(k)
                            if (!v.isNullOrBlank()) return v
                        }
                    }
                    val prevSaved = nav.previousBackStackEntry?.savedStateHandle
                    if (prevSaved != null) {
                        for (k in keys) {
                            val v = prevSaved.get<String>(k)
                            if (!v.isNullOrBlank()) return v
                        }
                    }
                    // finally try to look for a qna_origin map on any of these savedStateHandles
                    val originCandidates = listOf(ownSaved, currentSaved, prevSaved)
                    for (saved in originCandidates) {
                        if (saved == null) continue
                        try {
                            val origin = saved.get<Map<String, String>>("qna_origin")
                            if (origin != null) {
                                // try common keys
                                val candidate = origin["coachId"] ?: origin["classId"] ?: origin["id"]
                                if (!candidate.isNullOrBlank()) return candidate
                            }
                        } catch (_: Throwable) {
                        }
                    }
                    return null
                }

                val resolvedCoachId = when {
                    coachIdArg.isNotBlank() -> coachIdArg
                    else -> findSavedString("qna_coachId", "coachId", "qna_coachId") ?: ""
                }

                // Resolve date/time from saved handles if needed
                fun findSavedDateTime(): Pair<java.time.LocalDateTime, java.time.LocalDateTime>? {
                    // check saved for qna_date / qna_time or origin map
                    val dateStr = findSavedString("qna_date", "date")
                    val timeStr = findSavedString("qna_time", "time")
                    if (!dateStr.isNullOrBlank() && !timeStr.isNullOrBlank()) {
                        return try {
                            val h = timeTokenToHour(timeStr)
                            val s = java.time.LocalDateTime.of(java.time.LocalDate.parse(dateStr), java.time.LocalTime.of(h,0))
                            s to s.plusHours(1)
                        } catch (t: Throwable) {
                            null
                        }
                    }
                    return null
                }

                val resolvedStartEnd = findSavedDateTime()
                var resolvedStart = startAt
                var resolvedEnd = endAt
                if (resolvedStartEnd != null) {
                    resolvedStart = resolvedStartEnd.first
                    resolvedEnd = resolvedStartEnd.second
                } else {
                    // if coachId was resolved but date/time were stored differently, try to use backStackEntry saved
                    val altDate = findSavedString("qna_date","date")
                    val altTime = findSavedString("qna_time","time")
                    if (!altDate.isNullOrBlank() && !altTime.isNullOrBlank()) {
                        try {
                            val h = timeTokenToHour(altTime)
                            val s = java.time.LocalDateTime.of(java.time.LocalDate.parse(altDate), java.time.LocalTime.of(h,0))
                            resolvedStart = s
                            resolvedEnd = s.plusHours(1)
                        } catch (_: Throwable) {
                        }
                    }
                }

                if (resolvedCoachId.isBlank()) {
                    android.util.Log.e("MemberNavGraph", "Cannot resolve coachId for reservation; aborting reserveCoach call (coachIdArg='$coachIdArg')")
                } else {
                    android.util.Log.d("MemberNavGraph", "reserveCoach called from QnA (coachId=$resolvedCoachId, startAt=$resolvedStart, preQnA='${preQnA.take(60)}')")
                    reservationVmForQna.reserveCoach(resolvedCoachId, resolvedStart, resolvedEnd, preQnA)
                }
            },
            onNavigateHome = { nav.navigate(Routes.MemberHome) },
            // when user wants to change health info from reservation dialog, start the health survey flow
            onNavigateToMyHealthInfo = {
                // store a marker route so AppNavGraph can pop back reliably to this QnA entry after health flow
                try {
                    // Store qna_origin map directly on the current backStackEntry.savedStateHandle
                    val originMap = mapOf(
                        "type" to "coach",
                        "coachId" to coachIdArg,
                        "coachName" to decodedName,
                        "date" to dateStr,
                        "time" to decodedTime
                    )
                    nav.currentBackStackEntry?.savedStateHandle?.set("qna_origin", originMap)
                    android.util.Log.d("MemberNavGraph", "Set qna_origin on savedStateHandle=$originMap before navigating to health flow")
                } catch (t: Throwable) {
                    android.util.Log.d("MemberNavGraph", "Failed to set qna_origin: ${t.message}")
                }
                nav.navigate(com.livon.app.navigation.Routes.HealthHeight)
            },
            navController = nav,
            externalError = actionState.errorMessage
        )
    }

    // --- ADDED: accept coachName first (missing coachId) -> support routes like qna_submit/{coachName}/{date}/{time}
    composable("qna_submit/{coachName}/{date}/{time}") { backStackEntry ->
        val encodedName = backStackEntry.arguments?.getString("coachName") ?: ""
        val decodedName = try { URLDecoder.decode(encodedName, "UTF-8") } catch (t: Throwable) { encodedName }
        val coachIdArg = "" // no coachId provided in this route
        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
        val parsedDate = try { LocalDate.parse(dateStr) } catch (t: Throwable) { LocalDate.now() }
        val timeRaw = backStackEntry.arguments?.getString("time") ?: ""
        val decodedTime = try { URLDecoder.decode(timeRaw, "UTF-8") } catch (t: Throwable) { timeRaw }

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

        val reservationRepoForQna = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVmForQna = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoForQna) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val actionState by reservationVmForQna.actionState.collectAsState()
        LaunchedEffect(actionState.success) {
            if (actionState.success == true) {
                nav.navigate("reservations") { popUpTo(Routes.MemberHome) { inclusive = false } }
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

                android.util.Log.d("MemberNavGraph", "reserveCoach (no id route) called (coachId=$coachIdArg, startAt=$startAt)")
                reservationVmForQna.reserveCoach(coachIdArg, startAt, endAt, preQnA)
            },
            onNavigateHome = { nav.navigate(Routes.MemberHome) },
            onNavigateToMyHealthInfo = {
                try {
                    // Store qna_origin map directly on the current backStackEntry.savedStateHandle
                    val originMap = mapOf(
                        "type" to "coach",
                        "coachId" to coachIdArg,
                        "coachName" to decodedName,
                        "date" to dateStr,
                        "time" to decodedTime
                    )
                    nav.currentBackStackEntry?.savedStateHandle?.set("qna_origin", originMap)
                    android.util.Log.d("MemberNavGraph", "Set qna_origin on savedStateHandle=$originMap before navigating to health flow")
                } catch (t: Throwable) {
                    android.util.Log.d("MemberNavGraph", "Failed to set qna_origin: ${t.message}")
                }
                nav.navigate(com.livon.app.navigation.Routes.HealthHeight)
            },
            navController = nav,
            externalError = actionState.errorMessage
        )
    }

    composable("qna_submit/{coachId}/{coachName}/{date}") { backStackEntry ->
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
            if (actionState.success == true) {
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

                // Try to resolve coachId/startAt from savedStateHandle if nav-args were lost during health flow
                val resolvedCoachId = if (coachIdArg.isNotBlank()) coachIdArg else backStackEntry.savedStateHandle.get<String>("qna_coachId") ?: ""
                var resolvedStart = startAt
                var resolvedEnd = endAt
                if (resolvedCoachId.isBlank()) {
                    // attempt to reconstruct from saved handle date/time
                    val sDate = backStackEntry.savedStateHandle.get<String>("qna_date")
                    val sTime = backStackEntry.savedStateHandle.get<String>("qna_time")
                    if (!sDate.isNullOrBlank() && !sTime.isNullOrBlank()) {
                        try {
                            val h = timeTokenToHour(sTime)
                            resolvedStart = java.time.LocalDateTime.of(java.time.LocalDate.parse(sDate), java.time.LocalTime.of(h,0))
                            resolvedEnd = resolvedStart.plusHours(1)
                        } catch (t: Throwable) {
                            // keep previously computed startAt
                        }
                    }
                }

                if (resolvedCoachId.isBlank()) {
                    android.util.Log.e("MemberNavGraph", "Cannot resolve coachId for reservation; aborting reserveCoach call (coachIdArg='$coachIdArg')")
                } else {
                    android.util.Log.d("MemberNavGraph", "reserveCoach called from QnA (coachId=$resolvedCoachId, startAt=$resolvedStart, preQnA='${preQnA.take(60)}')")
                    reservationVmForQna.reserveCoach(resolvedCoachId, resolvedStart, resolvedEnd, preQnA)
                }
            },
            onNavigateHome = { nav.navigate(Routes.MemberHome) },
            // when user wants to change health info from reservation dialog, start the health survey flow
            onNavigateToMyHealthInfo = {
                // store a marker route so AppNavGraph can pop back reliably to this QnA entry after health flow
                try {
                    // Store qna_origin map directly on the current backStackEntry.savedStateHandle
                    val originMap = mapOf(
                        "type" to "coach",
                        "coachId" to coachIdArg,
                        "coachName" to decodedName,
                        "date" to dateStr,
                        "time" to decodedTime
                    )
                    nav.currentBackStackEntry?.savedStateHandle?.set("qna_origin", originMap)
                    android.util.Log.d("MemberNavGraph", "Set qna_origin on savedStateHandle=$originMap before navigating to health flow")
                } catch (t: Throwable) {
                    android.util.Log.d("MemberNavGraph", "Failed to set qna_origin: ${t.message}")
                }
                nav.navigate(com.livon.app.navigation.Routes.HealthHeight)
            },
            navController = nav,
            externalError = actionState.errorMessage
        )
    }

    // Reservation (예약 현황) screen
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

        // dialog state for cancel confirmation and join-missing
        val showCancelDialog = remember { mutableStateOf(false) }
        val showJoinMissingDialog = remember { mutableStateOf(false) }
        val selectedReservation = remember { mutableStateOf<ReservationUi?>(null) }

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
            // navigate to reservation detail by id
            onDetail = { item ->
                try {
                    nav.navigate("reservation_detail/${'$'}{item.id}")
                } catch (t: Throwable) {
                    // if navigation fails, fallback to no-op
                }
            },
            onCancel = { item ->
                selectedReservation.value = item
                showCancelDialog.value = true
            },
            onJoin = { item ->
                // If sessionId is null or empty, show alert dialog explaining coach hasn't created session yet.
                if (item.sessionId.isNullOrBlank()) {
                    selectedReservation.value = item
                    showJoinMissingDialog.value = true
                } else {
                    try {
                        nav.navigate("live_member/${'$'}{item.sessionId}")
                    } catch (t: Throwable) {
                        // ignore
                    }
                }
            },
            onAiAnalyze = { /* TODO: show AI analysis */ }
        )

        // Cancel confirmation dialog
        if (showCancelDialog.value && selectedReservation.value != null) {
            val sel = selectedReservation.value!!
            AlertDialog(
                onDismissRequest = { showCancelDialog.value = false; selectedReservation.value = null },
                title = { Text(text = "예약 취소") },
                text = { Text(text = "예약을 취소하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        // Optimistic UI update: remove from local cache and refresh UI immediately
                        val idInt = sel.id.toIntOrNull()
                        if (idInt != null) {
                            com.livon.app.data.repository.ReservationRepositoryImpl.localReservations.removeAll { it.id == idInt }
                            reservationVm.loadUpcoming()
                            if ((sel.sessionTypeLabel ?: "").contains("개인")) {
                                reservationVm.cancelIndividual(idInt)
                            } else {
                                reservationVm.cancelGroupParticipation(idInt)
                            }
                        }
                        showCancelDialog.value = false
                        selectedReservation.value = null
                    }) {
                        Text(text = "확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog.value = false; selectedReservation.value = null }) {
                        Text(text = "취소")
                    }
                }
            )
        }

        // Join-missing dialog: coach hasn't created live session yet
        if (showJoinMissingDialog.value && selectedReservation.value != null) {
            val sel = selectedReservation.value!!
            AlertDialog(
                onDismissRequest = { showJoinMissingDialog.value = false; selectedReservation.value = null },
                title = { Text(text = "세션 준비중") },
                text = { Text(text = "아직 코치가 상담 세션을 생성하지 않았습니다.") },
                confirmButton = {
                    TextButton(onClick = {
                        showJoinMissingDialog.value = false
                        selectedReservation.value = null
                    }) {
                        Text(text = "확인")
                    }
                }
            )
        }

    }

    // Reservation detail route: show details for a specific reservation id
    composable("reservation_detail/{id}") { backStackEntry ->
        val id = backStackEntry.arguments?.getString("id") ?: ""

        // reuse ReservationViewModel to read current list (it will fetch from server)
        val reservationRepoDetail = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVmDetail = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoDetail) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val stateDetail by reservationVmDetail.uiState.collectAsState()
        // ensure we have the latest items
        LaunchedEffect(Unit) { reservationVmDetail.loadUpcoming() }

        // find reservation by id
        val found = stateDetail.items.find { it.id == id }

        if (found != null) {
            // derive detail type
            val detailType = when {
                found.isLive -> ReservationDetailType.Current
                (found.sessionTypeLabel ?: "").contains("개인") -> ReservationDetailType.PastPersonal
                else -> ReservationDetailType.PastGroup
            }

            val coachMini = CoachMini(
                name = found.coachName.ifEmpty { "코치" },
                title = found.coachRole.ifEmpty { "" },
                specialties = found.coachIntro.ifEmpty { "" },
                workplace = ""
            )

            val sessionInfo = SessionInfo(
                dateText = "${found.date.monthValue}월 ${found.date.dayOfMonth}일",
                timeText = found.timeText,
                modelText = found.className,
                appliedText = null
            )

            ReservationDetailScreen(
                type = detailType,
                coach = coachMini,
                session = sessionInfo,
                aiSummary = null,
                qnas = emptyList(),
                onBack = { nav.popBackStack() },
                onDelete = { /* TODO: delete past reservation from server/local */ },
                onSeeCoach = { /* TODO: navigate to coach detail if id available */ },
                onSeeAiDetail = { /* TODO */ }
            )
        } else {
            // show loading / not found
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "예약 정보를 불러오는 중입니다...")
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Live member screen: entry point for members to join LiveKit session
    composable("live_member/{sessionId}") { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
        // For now show a simple placeholder screen; wiring livekit is out of scope here
        com.livon.app.feature.member.streaming.ui.LiveStreamingMemberScreen() // implement minimal composable
    }
}
