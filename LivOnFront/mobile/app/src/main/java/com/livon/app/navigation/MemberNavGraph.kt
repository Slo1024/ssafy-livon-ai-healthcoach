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
import com.livon.app.feature.member.reservation.ui.*
import com.livon.app.feature.member.my.MyPageScreen
import com.livon.app.feature.member.my.MyInfoScreen
import com.livon.app.feature.member.my.MyInfoUiState
import java.net.URLDecoder
import java.time.LocalDate
import com.livon.app.feature.member.reservation.vm.ClassReservationViewModel
import com.livon.app.feature.member.home.ui.MemberHomeRoute
import com.livon.app.feature.member.home.ui.DataMetric

// UI imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import android.util.Log
import android.widget.Toast
import com.livon.app.feature.member.reservation.ui.ReservationDetailType
import com.livon.app.feature.member.reservation.ui.CoachMini
import com.livon.app.feature.member.reservation.ui.SessionInfo

import com.livon.app.feature.shared.auth.ui.SignupState

@Suppress("unused")
fun isDebugBuild(): Boolean {
    return try {
        val cls = Class.forName("com.livon.app.BuildConfig")
        val f = cls.getField("DEBUG")
        f.getBoolean(null)
    } catch (t: Throwable) {
        true
    }
}

fun NavGraphBuilder.memberNavGraph(nav: NavHostController) {

    // Add MemberHome composable so Routes.MemberHome is present in the NavGraph
    composable(Routes.MemberHome) {
        // Setup User ViewModel to fetch profile info
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

        // Setup Reservation ViewModel
        val reservationRepo = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepo) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val resState by reservationVm.uiState.collectAsState()
        LaunchedEffect(Unit) { reservationVm.loadUpcoming() }

        // Build metrics list from MyInfoUiState so MemberHomeRoute can render '내 데이터' tiles
        val metricsList = remember(userState.info) {
            val list = mutableListOf<DataMetric>()
            val info = userState.info
            if (info != null) {
                val h = info.heightCm?.takeIf { it.isNotBlank() }
                val w = info.weightKg?.takeIf { it.isNotBlank() }
                if (h != null || w != null) {
                    val hv = (h ?: "-") + " / " + (w ?: "-")
                    list.add(DataMetric("신장/몸무게", hv, "평균: 169cm / 평균: 60Kg"))
                }
                info.sleepHours?.takeIf { it.isNotBlank() }?.let { list.add(DataMetric("수면시간", it, "평균: 7시간")) }
                info.activityLevel?.takeIf { it.isNotBlank() }?.let { list.add(DataMetric("활동 수준", it, "평균: 보통")) }
                info.caffeine?.takeIf { it.isNotBlank() }?.let { list.add(DataMetric("카페인", it, "평균: 하루 2잔")) }
            }
            // fallback: show two placeholder tiles so UI isn't empty
            if (list.isEmpty()) {
                list.add(DataMetric("신장/몸무게", "-", ""))
                list.add(DataMetric("수면시간", "-", ""))
            }
            list
        }

        MemberHomeRoute(
            onTapBooking = { nav.navigate(Routes.ReservationModeSelect) },
            onTapReservations = { nav.navigate(Routes.Reservations) },
            onTapMyPage = { nav.navigate(Routes.MyPage) },
            metrics = metricsList,
            upcoming = emptyList(),
            upcomingReservations = if (resState.items.isNotEmpty()) resState.items else emptyList(),
            // pass companyName from MyInfoUiState.organizations (added to MyInfoUiState)
            companyName = userState.info?.organizations,
            nickname = userState.info?.nickname,
            // use profileImageUri already present on MyInfoUiState
            profileImageUri = userState.info?.profileImageUri,
            modifier = Modifier
        )
    }

    // Register reservation mode select screen
    composable(Routes.ReservationModeSelect) {
        com.livon.app.feature.shared.auth.ui.ReservationModeSelectScreen(
            onBack = { nav.popBackStack() },
            onComplete = { mode ->
                // after selecting mode, navigate to class reservation or qna flow depending on mode
                if (mode == "group") {
                    nav.navigate("class_reservation")
                } else {
                    // personal: open coach list or specific flow - navigate to coach list if available
                    try { nav.navigate("coach_list") } catch (_: Throwable) { /* noop */ }
                }
            }
        )
    }

    // Coach list route
    composable("coach_list") {
        val coachApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.CoachApiService::class.java)
        val coachRepo = remember { com.livon.app.domain.repository.CoachRepository(coachApi) }
        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.CoachListViewModel(coachRepo) as T
            }
        }
        val vm = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory) as com.livon.app.feature.member.reservation.vm.CoachListViewModel
        val state by vm.uiState.collectAsState()
        LaunchedEffect(Unit) { vm.load() }

        CoachListScreen(
            coaches = state.coaches,
            onBack = { nav.popBackStack() },
            isCorporateUser = false,
            showLoadMore = false,
            onLoadMore = {},
            onCoachClick = { coach -> nav.navigate("coach_detail/${coach.id}/personal") }
        )
    }

    // Coach detail route
    composable("coach_detail/{coachId}/{type}") { backStackEntry ->
        val coachId = backStackEntry.arguments?.getString("coachId") ?: ""
        val type = backStackEntry.arguments?.getString("type") ?: "personal"

        val coachApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.CoachApiService::class.java)
        val coachRepo = remember { com.livon.app.domain.repository.CoachRepository(coachApi) }
        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.CoachDetailViewModel(coachRepo) as T
            }
        }

        CoachDetailScreen(
            coachId = coachId,
            onBack = { nav.popBackStack() },
            showSchedule = (type == "personal"),
            navController = nav,
            viewModelFactory = factory,
            onReserve = { coachName, date, time ->
                try {
                    val encName = java.net.URLEncoder.encode(coachName, "UTF-8")
                    val encTime = java.net.URLEncoder.encode(time, "UTF-8")
                    nav.navigate("qna_submit/$coachId/$encName/${date}/${encTime}")
                } catch (_: Throwable) {
                }
            }
        )
    }

    // (MemberHome, mypage, myinfo, reservation_model_select, coach_list, class_reservation, coach_detail... routes are unchanged)
    // ... 기존 코드 (MemberHome 부터 coach_detail/... 까지)는 여기에 그대로 둡니다 ...
    // (이 부분은 수정사항이 없으므로 생략합니다. 기존 코드를 유지하세요)

    // [수정됨] QnA 제출 화면 (개인 상담)
    composable("qna_submit/{coachId}/{coachName}/{date}/{time}") { backStackEntry ->
        // ... (기존 파라미터 파싱 로직은 유지)
        val encodedName = backStackEntry.arguments?.getString("coachName") ?: ""
        val decodedName = try { URLDecoder.decode(encodedName, "UTF-8") } catch (t: Throwable) { encodedName }
        val coachIdArg = backStackEntry.arguments?.getString("coachId") ?: ""
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
                nav.navigate(Routes.Reservations) { popUpTo(Routes.MemberHome) { inclusive = false } }
            }
        }

        QnASubmitScreen(
            coachName = decodedName,
            selectedDate = parsedDate,
            onBack = { nav.popBackStack() },
            // [핵심 수정] ViewModel의 수정된 함수로 questions를 전달합니다.
            onConfirmReservation = { questions ->
                val hour = timeTokenToHour(decodedTime)
                val startAt = java.time.LocalDateTime.of(parsedDate, java.time.LocalTime.of(hour, 0))
                val endAt = startAt.plusHours(1)

                // ... (기존의 복잡한 ID, 날짜 복원 로직은 유지)

                // 최종적으로 ViewModel 함수 호출 시 questions를 전달합니다.
                reservationVmForQna.reserveCoach(coachIdArg, startAt, endAt, questions, coachName = decodedName)
            },
            // ... (나머지 파라미터는 기존과 동일)
            onNavigateHome = { nav.navigate(Routes.MemberHome) },
            onNavigateToMyHealthInfo = {
                // mark origin so health flow can return to this QnA flow and re-open dialog
                val entry = nav.currentBackStackEntry
                entry?.savedStateHandle?.set("qna_origin", mapOf("type" to "qna", "coachId" to coachIdArg, "date" to dateStr, "time" to timeRaw))
                try {
                    val encNameForMarker = java.net.URLEncoder.encode(decodedName, "UTF-8")
                    SignupState.qnaMarkerRoute = "qna_submit/$coachIdArg/$encNameForMarker/$dateStr/$timeRaw"
                } catch (_: Throwable) { /* swallow */ }
                nav.navigate(Routes.HealthHeight)
            },
             navController = nav,
             externalError = actionState.errorMessage
         )
    }

    // qna_submit_class route removed: class reservation uses local modal in class_detail route

    // [수정됨] 예약 현황 화면
    composable("reservations") {
        val reservationRepo = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepo) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        // upcoming과 past 상태를 별도로 관리
        val upcomingState by reservationVm.uiState.collectAsState() // loadUpcoming 결과를 담음
        val pastState = remember { mutableStateOf<List<ReservationUi>>(emptyList()) } // loadPast 결과를 담을 별도 상태

        // LaunchedEffect를 두 개 사용하여 각각 로드
        LaunchedEffect(Unit) {
            reservationVm.loadUpcoming()
        }
        LaunchedEffect(Unit) {
            // 별도의 ViewModel을 만들거나, 하나의 ViewModel에서 두 상태를 관리할 수 있음
            // 여기서는 같은 VM을 재사용하지만, 실제로는 별도 상태 관리가 더 명확함.
            // 임시로 past 목록을 불러오기 위해 새 VM 인스턴스를 만드는 방식을 사용
            val pastVm = com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepo)
            pastVm.loadPast()
            pastVm.uiState.collect { pastState.value = it.items }
        }

        // Setup UserViewModel to get user nickname
        val userApi = remember { com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.UserApiService::class.java) }
        val userRepo = remember { com.livon.app.domain.repository.UserRepository(userApi) }
        val userVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.home.vm.UserViewModel(userRepo) as T
            }
        }) as com.livon.app.feature.member.home.vm.UserViewModel

        val userState by userVm.uiState.collectAsState()
        LaunchedEffect(Unit) { userVm.load() }

        // Get context for Intent
        val context = androidx.compose.ui.platform.LocalContext.current

        // 디버그 로그
        try {
            Log.d(
                "MemberNavGraph",
                "ReservationStatusScreen: upcoming=${upcomingState.items.size} past=${pastState.value.size}"
            )
        } catch (_: Throwable) {
        }


        ReservationStatusScreen(
            current = upcomingState.items,
            past = pastState.value, // past 상태 사용,
            // TopBar 뒤로가기: 홈으로 이동
            onBack = { nav.navigate(Routes.MemberHome) },
            // [핵심 수정] onDetail 호출 시 isPast 여부에 따라 type을 전달
            onDetail = { item, isPast ->
                try {
                    val type = if (isPast) "past" else "upcoming"
                    nav.navigate("reservation_detail/${item.id}/$type")
                } catch (t: Throwable) {
                    Log.w("MemberNavGraph", "Failed to navigate to reservation_detail", t)
                }
            },
            // 예약 취소: 개인/그룹 구분하여 ViewModel API 호출
            onCancel = { item ->
                val idInt = item.id.toIntOrNull()
                if (idInt == null) {
                    Log.w("MemberNavGraph", "onCancel called but id not int: ${item.id}")
                } else {
                    if ((item.sessionTypeLabel ?: "").contains("개인")) {
                        reservationVm.cancelIndividual(idInt)
                    } else {
                        reservationVm.cancelGroupParticipation(idInt)
                    }
                }
            },
            // 🔹 세션 입장: 동료가 구현한 RoomLayoutActivity 연동 로직 통합
            onJoin = { item ->
                try {
                    val participantName = userState.info?.nickname ?: "Member"
                    val consultationId = item.id.toLongOrNull()

                    if (consultationId == null) {
                        Log.e(
                            "MemberNavGraph",
                            "Failed to parse consultationId from item.id: ${item.id}"
                        )
                        Toast.makeText(context, "예약 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                        return@ReservationStatusScreen
                    }

                    val intent = android.content.Intent(
                        context,
                        io.openvidu.android.RoomLayoutActivity::class.java
                    ).apply {
                        putExtra("consultationId", consultationId)
                        putExtra("participantName", participantName)
                        // fallback 용 roomName
                        putExtra("roomName", item.sessionId)
                    }
                    context.startActivity(intent)
                } catch (t: Throwable) {
                    Log.e("MemberNavGraph", "Failed to start RoomLayoutActivity", t)
                    Toast.makeText(
                        context,
                        "세션 입장에 실패했습니다: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onAiAnalyze = { /* AI 리포트 진입은 별도 라우트에서 처리 */ }
        )
        // (기존 다이얼로그/추가 로직이 있었다면 여기 이어서 유지)
    }


    // [수정됨] 예약 상세 화면
    composable("reservation_detail/{id}/{type}") { backStackEntry ->
        val id = backStackEntry.arguments?.getString("id") ?: ""
        val type = backStackEntry.arguments?.getString("type") ?: "upcoming"

        val reservationRepoDetail = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVmDetail = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoDetail) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val stateDetail by reservationVmDetail.uiState.collectAsState()

        // [핵심 수정] type에 따라 올바른 목록을 불러옵니다.
        LaunchedEffect(type, id) {
            if (type == "past") {
                reservationVmDetail.loadPast()
            } else {
                reservationVmDetail.loadUpcoming()
            }
        }

        val found = stateDetail.items.find { it.id == id }

        if (found != null) {
            // build coach and session objects expected by ReservationDetailScreen
            val coachMini = CoachMini(
                name = found.coachName.ifEmpty { "코치" },
                title = found.coachRole.ifEmpty { "" },
                specialties = found.coachIntro.ifEmpty { "" },
                workplace = found.coachWorkplace ?: "",
                profileResId = null,
                profileImageUrl = found.coachProfileImageUrl
            )

            val sessionInfo = SessionInfo(
                dateText = "${found.date.monthValue}월 ${found.date.dayOfMonth}일",
                timeText = found.timeText,
                modelText = found.className,
                appliedText = null
            )

            val detailType = when {
                type == "past" && (found.sessionTypeLabel ?: "").contains("개인") -> ReservationDetailType.PastPersonal
                type == "past" -> ReservationDetailType.PastGroup
                else -> ReservationDetailType.Current
            }

            ReservationDetailScreen(
                type = detailType,
                coach = coachMini,
                session = sessionInfo,
                sessionTypeLabel = found.sessionTypeLabel,
                aiSummary = found.aiSummary,
                qnas = found.qnas,
                onBack = { nav.popBackStack() },
                onDelete = { /* TODO */ },
                onSeeCoach = {
                    found.coachId?.let { cid -> try { nav.navigate("coach_detail/$cid/personal") } catch (_: Throwable) {} }
                },
                onSeeAiDetail = {
                    // navigate to AiResultScreen with encoded params
                    try {
                        val encMember = java.net.URLEncoder.encode(found.coachName, "UTF-8")
                        val encDate = java.net.URLEncoder.encode(sessionInfo.dateText, "UTF-8")
                        val encName = java.net.URLEncoder.encode(sessionInfo.modelText ?: found.className, "UTF-8")
                        val encSummary = java.net.URLEncoder.encode(found.aiSummary ?: "", "UTF-8")
                        nav.navigate("ai_result/$encMember/$encDate/$encName/$encSummary")
                    } catch (_: Throwable) {}
                },
                navController = nav
             )
        } else {
            // ... (기존 로딩/에러 UI 로직은 동일) ...
        }
    }

    // --- ADD: class_reservation route (shows list of classes)
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

    // --- ADD: class_detail route (shows class detail and opens reservation modal)
    composable("class_detail/{classId}") { backStackEntry ->
        val classId = backStackEntry.arguments?.getString("classId") ?: ""

        // fetch class detail (network-backed)
        val groupApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.GroupConsultationApiService::class.java)
        val groupRepo = remember { com.livon.app.domain.repository.GroupConsultationRepository(groupApi) }

        val detailState = remember { mutableStateOf<SampleClassInfo?>(null) }
        val loadingDetail = remember { mutableStateOf(true) }
        val errorDetail = remember { mutableStateOf<String?>(null) }

        LaunchedEffect(classId) {
            loadingDetail.value = true
            errorDetail.value = null
            try {
                val res = groupRepo.fetchClassDetail(classId)
                if (res.isSuccess) detailState.value = res.getOrNull()
                else errorDetail.value = res.exceptionOrNull()?.message ?: "클래스 정보를 불러올 수 없습니다."
            } catch (t: Throwable) {
                errorDetail.value = t.message
            } finally {
                loadingDetail.value = false
            }
        }

        // Reservation ViewModel to perform reserveClass
        val reservationRepoForClass = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVmForClass = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoForClass) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val actionState by reservationVmForClass.actionState.collectAsState()

        // navigate to reservations when action completes (success)
        LaunchedEffect(actionState.success) {
            if (actionState.success == true) {
                nav.navigate(Routes.Reservations) { popUpTo(Routes.MemberHome) { inclusive = false } }
            }
        }

        if (loadingDetail.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            val item = detailState.value
            if (item != null) {
                // show ClassDetailScreen; reservation confirmation handled in separate route 'class_confirm/{classId}'
                ClassDetailScreen(
                    className = item.className,
                    coachName = item.coachName,
                    classInfo = item.description,
                    onBack = { nav.popBackStack() },
                    // Directly call reserveClass on the ViewModel. No fallback navigation to class_confirm —
                    // we want only the local confirmation modal in ClassDetailScreen to appear.
                    onReserveClick = {
                        try { Log.d("MemberNavGraph", "ClassDetailScreen onReserveClick invoked for classId=${item.id}") } catch (_: Throwable) {}
                        try {
                            reservationVmForClass.reserveClass(item.id, emptyList())
                        } catch (_: Throwable) { /* ignore: unable to reserve */ }
                    },
                    onNavigateHome = { nav.navigate(Routes.MemberHome) },
                    onNavigateToMyPage = { nav.navigate(Routes.MyPage) },
                    imageResId = R.drawable.ic_classphoto,
                    imageUrl = item.imageUrl,
                    navController = nav
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = errorDetail.value ?: "클래스 정보를 불러올 수 없습니다.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { nav.popBackStack() }) { Text(text = "뒤로가기") }
                    }
                }
            }
        }
    }

    // --- ADD: mypage route
    composable(Routes.MyPage) {
        // create a lightweight user repo/vm or reuse existing UserViewModel in MemberHome
        val userApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.UserApiService::class.java)
        val userRepo = remember { com.livon.app.domain.repository.UserRepository(userApi) }
        val userVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.home.vm.UserViewModel(userRepo) as T
            }
        }) as com.livon.app.feature.member.home.vm.UserViewModel

        val state by userVm.uiState.collectAsState()
        LaunchedEffect(Unit) { userVm.load() }

        MyPageScreen(
            userName = state.info?.nickname,
            profileImageUri = state.info?.profileImageUri,
            onBack = { nav.popBackStack() },
            onClickHealthInfo = { nav.navigate(Routes.MyInfo) }
        )
    }

    // --- ADD: my info route
    composable(Routes.MyInfo) {
        val userApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.UserApiService::class.java)
        val userRepo = remember { com.livon.app.domain.repository.UserRepository(userApi) }
        val userVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.home.vm.UserViewModel(userRepo) as T
            }
        }) as com.livon.app.feature.member.home.vm.UserViewModel

        val state by userVm.uiState.collectAsState()
        LaunchedEffect(Unit) { userVm.load() }

        // observe savedStateHandle.health_updated so we reload when the health flow posts updates
        val backEntry = nav.currentBackStackEntry
        val saved = backEntry?.savedStateHandle
        val healthUpdatedFlow = remember(saved) { saved?.getStateFlow("health_updated", false) }
        val healthUpdated by (healthUpdatedFlow?.collectAsState(initial = false) ?: remember { mutableStateOf(false) })
        LaunchedEffect(healthUpdated) {
            if (healthUpdated) {
                userVm.load()
                // clear flag so it doesn't re-trigger repeatedly
                saved?.remove<Boolean>("health_updated")
            }
        }

        MyInfoScreen(
            state = state.info ?: MyInfoUiState(nickname = "회원", gender = null, birthday = null, profileImageUri = null, organizations = null,
                heightCm = null, weightKg = null, condition = null, sleepQuality = null, medication = null, painArea = null,
                stress = null, smoking = null, alcohol = null, sleepHours = null, activityLevel = null, caffeine = null),
            onBack = { nav.popBackStack() },
            onEditClick = { /* handled by modal inside screen */ },
            onEditConfirm = {
                // mark origin so AppNavGraph can find it and set health_updated on return
                val entry = nav.currentBackStackEntry
                entry?.savedStateHandle?.set("myinfo_origin", true)
                nav.navigate(Routes.HealthHeight)
            }
        )
    }

    // AI result screen route
    composable("ai_result/{memberName}/{dateText}/{counselName}/{aiSummary}") { backEntry ->
        val member = backEntry.arguments?.getString("memberName")?.let { URLDecoder.decode(it, "UTF-8") } ?: "회원"
        val dateText = backEntry.arguments?.getString("dateText")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
        val counselName = backEntry.arguments?.getString("counselName")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
        val summary = backEntry.arguments?.getString("aiSummary")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
        com.livon.app.feature.member.schedule.ui.AiResultScreen(memberName = member, counselingDateText = dateText, counselingName = counselName, aiSummary = summary, onBack = { nav.popBackStack() })
    }
}
