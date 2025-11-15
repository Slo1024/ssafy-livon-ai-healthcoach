// com/livon/app/navigation/MemberNavGraph.kt
package com.livon.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import com.livon.app.R
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.livon.app.feature.member.reservation.ui.*
import com.livon.app.feature.shared.auth.ui.SignupState
import com.livon.app.feature.member.home.ui.MemberHomeRoute
import com.livon.app.feature.member.home.ui.DataMetric
import com.livon.app.feature.member.reservation.vm.ClassReservationViewModel
import com.livon.app.feature.member.reservation.ui.SampleClassInfo
import androidx.compose.ui.platform.LocalContext
import java.net.URLDecoder
import java.time.LocalDate
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import kotlinx.coroutines.launch

// UI imports
import androidx.compose.ui.Modifier

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
        val ctxForReservation = LocalContext.current
        // Wait for session token to be ready, then load persisted reservations for this user
        val sessionToken by com.livon.app.data.session.SessionManager.token.collectAsState()
        LaunchedEffect(sessionToken) {
            if (!sessionToken.isNullOrBlank()) {
                try { reservationRepo.loadPersistedReservations(ctxForReservation) } catch (_: Throwable) {}
            }
        }
        val reservationVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepo) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val resState by reservationVm.uiState.collectAsState()
        val actionStateGlobal by reservationVm.actionState.collectAsState()
        // After loading persisted reservations (and after login), refresh upcoming from server
        LaunchedEffect(sessionToken) {
            if (!sessionToken.isNullOrBlank()) {
                reservationVm.loadUpcoming()
            }
        }
        // Persist localReservations whenever an action (reserve/cancel) succeeds
        LaunchedEffect(actionStateGlobal.success) {
            if (actionStateGlobal.success == true) {
                try { reservationRepo.persistLocalReservations(ctxForReservation) } catch (_: Throwable) {}
            }
        }

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
        val ctxQna = LocalContext.current
        val reservationVmForQna = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoForQna) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val actionState by reservationVmForQna.actionState.collectAsState()



        // [수정] 예약 성공 후 즉시 로컬 캐시 동기화 및 예약 현황 화면으로 이동
        // reservations 화면에서 사용할 수 있도록 reservationCreatedFlow를 전달해야 하지만,
        // 여기서는 직접 예약 현황 화면으로 이동하기 전에 loadUpcoming을 호출하도록 개선
        LaunchedEffect(actionState.success) {
            if (actionState.success == true) {
                try { 
                    // 서버와 동기화하여 최신 예약 정보 가져오기
                    reservationRepoForQna.syncFromServerAndPersist(ctxQna) 
                } catch (_: Throwable) {}
                try { 
                    // 로컬 캐시 저장
                    reservationRepoForQna.persistLocalReservations(ctxQna) 
                } catch (_: Throwable) {}
                // 예약 현황 화면으로 이동 (서버 동기화 후 약간의 지연을 두어 데이터가 준비되도록 함)
                kotlinx.coroutines.delay(500) // 서버 동기화 및 localReservationsFlow emit 완료 대기
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
        val ctxForReservation = LocalContext.current
        
        // 세션 토큰을 확인하여 로그인 상태에서만 데이터 로드
        val sessionToken by com.livon.app.data.session.SessionManager.token.collectAsState()
        LaunchedEffect(sessionToken) {
            if (!sessionToken.isNullOrBlank()) {
                try { reservationRepo.loadPersistedReservations(ctxForReservation) } catch (_: Throwable) {}
            }
        }
        
        val reservationVm = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepo) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        // upcoming과 past 상태를 별도로 관리
        val upcomingState by reservationVm.uiState.collectAsState() // loadUpcoming 결과를 담음
        val pastState = remember { mutableStateOf<List<ReservationUi>>(emptyList()) } // loadPast 결과를 담을 별도 상태

        // 세션 토큰이 있을 때만 데이터 로드 (앱 재시작 후에도 로드되도록)
        LaunchedEffect(sessionToken) {
            if (!sessionToken.isNullOrBlank()) {
                reservationVm.loadUpcoming()
            }
        }
        
        // past 목록을 별도로 관리하기 위한 별도 ViewModel 인스턴스
        val pastVm = remember { com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepo) }
        val pastVmState by pastVm.uiState.collectAsState()
        
        LaunchedEffect(sessionToken) {
            if (!sessionToken.isNullOrBlank()) {
                pastVm.loadPast()
            }
        }
        
        // past 상태 업데이트
        LaunchedEffect(pastVmState.items) {
            pastState.value = pastVmState.items
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
        val context = LocalContext.current
        
        // [수정] 예약 취소 성공 시 목록 갱신 + 예약 생성 후 화면 진입 시에도 목록 갱신
        val actionState by reservationVm.actionState.collectAsState()
        
        // [수정] 화면 진입 시에도 최신 예약 목록을 로드하도록 개선
        // 예약 생성 후 예약 현황 화면으로 이동할 때 데이터가 갱신되도록 함
        LaunchedEffect(Unit) {
            try {
                com.livon.app.data.repository.ReservationRepositoryImpl.localReservationsFlow.collect {
                    // 로컬 캐시가 업데이트되면 예약 목록을 다시 로드 (debounce 적용)
                    kotlinx.coroutines.delay(400) // 서버 동기화 완료를 위한 지연
                    if (!sessionToken.isNullOrBlank()) {
                        reservationVm.loadUpcoming()
                        pastVm.loadPast()
                    }
                }
            } catch (_: Throwable) { /* ignore collection errors */ }
        }
        
        LaunchedEffect(actionState.success) {
            if (actionState.success == true) {
                // 취소 성공 시 목록 갱신
                reservationVm.loadUpcoming()
                pastVm.loadPast()
                // 로컬 저장소에도 반영
                try { reservationRepo.persistLocalReservations(ctxForReservation) } catch (_: Throwable) {}
            }
        }

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
                    if ((item.sessionTypeLabel ?: "").contains("개인") || item.isPersonal) {
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
        val sessionTokenDetail by com.livon.app.data.session.SessionManager.token.collectAsState()

        // [핵심 수정] type에 따라 올바른 목록을 불러옵니다. 세션 토큰이 있을 때만 로드
        LaunchedEffect(type, id, sessionTokenDetail) {
            if (!sessionTokenDetail.isNullOrBlank()) {
                if (type == "past") {
                    reservationVmDetail.loadPast()
                } else {
                    reservationVmDetail.loadUpcoming()
                }
            }
        }

        // 로딩 중이거나 찾을 수 없는 경우를 처리
        // 먼저 현재 상태에서 찾아보고, 없으면 로딩 완료까지 대기
        val found = stateDetail.items.find { it.id == id }
        val isLoading = stateDetail.isLoading && found == null && stateDetail.items.isEmpty()

        // 로딩 중일 때는 로딩 화면 표시
        if (isLoading) {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (found != null) {
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
        } else if (!isLoading) {
            // 데이터를 찾을 수 없을 때 이전 화면으로 돌아가기
            LaunchedEffect(Unit) {
                nav.popBackStack()
            }
            // 로딩 화면 표시 (popBackStack이 완료될 때까지)
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
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

        val classesToShow = if (vmState.items.isNotEmpty()) vmState.items else emptyList<SampleClassInfo>()

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

        // Reservation VM for class reservation
        val reservationRepoForClass = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val reservationVmForClass = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.member.reservation.vm.ReservationViewModel(reservationRepoForClass) as T
            }
        }) as com.livon.app.feature.member.reservation.vm.ReservationViewModel

        val actionStateByClass by reservationVmForClass.actionState.collectAsState()
        val ctx = LocalContext.current

        // Navigate to reservations when class reservation succeeds or when repository indicates ALREADY_RESERVED
        LaunchedEffect(actionStateByClass.success, actionStateByClass.errorMessage) {
            val err = actionStateByClass.errorMessage ?: ""
            try {
                if (actionStateByClass.success == true) {
                    try {
                        try { reservationRepoForClass.syncFromServerAndPersist(ctx) } catch (_: Throwable) {}
                        try { reservationRepoForClass.persistLocalReservations(ctx) } catch (_: Throwable) {}
                        nav.navigate(Routes.Reservations) { popUpTo(Routes.MemberHome) { inclusive = false } }
                    } catch (_: Throwable) { /* ignore navigation errors */ }
                } else if (actionStateByClass.success == false && (err.contains("이미 예약") || err.contains("ALREADY_RESERVED") || err.contains("이미 예약된"))) {
                    // refresh will have been called by VM; navigate to Reservations so user sees it
                    try {
                        nav.navigate(Routes.Reservations) { popUpTo(Routes.MemberHome) { inclusive = false } }
                    } catch (_: Throwable) { /* ignore */ }
                    try {
                        Toast.makeText(ctx, "이미 예약된 클래스입니다.", Toast.LENGTH_SHORT).show()
                    } catch (_: Throwable) { /* ignore */ }
                }
            } catch (_: Throwable) {
                // ignore
            }
        }

        // check if coming back from health flow and modal should be re-opened
        val reopenDialog = backStackEntry.savedStateHandle.get<Boolean>("health_updated") ?: false
        // clear the flag so it doesn't reopen every time
        try { backStackEntry.savedStateHandle.remove<Boolean>("health_updated") } catch (_: Throwable) {}

        ClassDetailScreen(
               className = detailState.value?.className ?: "",
               coachName = detailState.value?.coachName ?: "",
               classInfo = detailState.value?.description ?: "",
               onBack = { nav.popBackStack() },
               onReserveClick = {
                   try { Log.d("MemberNavGraph", "ClassDetailScreen onReserveClick -> reserveClass for classId=$classId") } catch (_: Throwable) {}
                   // call reserveClass with empty qnas for now (ClassDetail doesn't collect qnas)
                   reservationVmForClass.reserveClass(classId, emptyList())
               },
             isSubmitting = (actionStateByClass.isLoading == true),
             onChangeHealthInfo = {
                 // set marker so health flow can return here and reopen dialog
                 try { SignupState.qnaMarkerRoute = "class_detail/$classId" } catch (_: Throwable) {}
                 try { nav.navigate(Routes.HealthHeight) } catch (_: Throwable) {}
             },
             initialShowReserveDialog = reopenDialog,
             onNavigateHome = { nav.navigate(Routes.MemberHome) },
             onNavigateToMyPage = { nav.navigate(Routes.MyPage) },
             imageResId = R.drawable.ic_classphoto,
             imageUrl = detailState.value?.imageUrl ?: "",
             navController = nav,
             // pass shared vm so screens can reuse cached reservations
             // (screens which accept it will use it; others ignore)
             // Note: reservationVmForClass remains for the actual reserveClass call to avoid interfering with shared vm
         )
    }

    // --- ADD: My Page route so HomeNavBar -> MyPage navigation works ---
    composable(Routes.MyPage) {
        // create user api/repo/vm similar to other screens
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

        // Context and reservation repo for logout cleanup
        val contextForMyPage = LocalContext.current
        val reservationRepoForLogout = remember { com.livon.app.data.repository.ReservationRepositoryImpl() }
        val coroutineScope = rememberCoroutineScope()

        // create authViewModel for logout handling
        val authApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.AuthApiService::class.java)
        val authRepo = remember { com.livon.app.domain.repository.AuthRepository(authApi) }
        val authFactory = remember {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return com.livon.app.feature.member.auth.vm.AuthViewModel(authRepo) as T
                }
            }
        }
        val authViewModel: com.livon.app.feature.member.auth.vm.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = authFactory)

        com.livon.app.feature.member.my.MyPageScreen(
            userName = userState.info?.nickname,
            profileImageUri = userState.info?.profileImageUri,
            onBack = { nav.popBackStack() },
            onClickHealthInfo = { try { nav.navigate(Routes.MyInfo) } catch (_: Throwable) {} },
            onClickFaq = { /* TODO: navigate to FAQ if exists */ },

            onLogoutConfirm = {
                // capture current token before clearing it in AuthViewModel
                val ownerToken = com.livon.app.data.session.SessionManager.getTokenSync()
                try { authViewModel.logout() } catch (_: Throwable) {}
                // clear persisted and in-memory reservations for that owner
                try {
                    coroutineScope.launch {
                        try { reservationRepoForLogout.clearLocalReservationsForOwner(ownerToken) } catch (_: Throwable) {}
                        try { reservationRepoForLogout.clearPersistedReservationsForOwner(contextForMyPage, ownerToken) } catch (_: Throwable) {}
                    }
                } catch (_: Throwable) {}
                try {
                    nav.navigate(Routes.EmailLogin) {
                        popUpTo(Routes.Landing) { inclusive = true }
                    }
                } catch (_: Throwable) {
                    try { nav.navigate(Routes.Landing) } catch (_: Throwable) {}
                }
            }
        )
    }

    // My Info route: shows the user's health info screen
    composable(Routes.MyInfo) {
        // create user api/repo/vm similar to other screens
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

        val info = userState.info
        // Provide a fallback state while loading
        val stateForUi = info ?: com.livon.app.feature.member.my.MyInfoUiState(
            nickname = userState.info?.nickname ?: "회원님",
            gender = null,
            birthday = null,
            profileImageUri = null,
            organizations = null,
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

        com.livon.app.feature.member.my.MyInfoScreen(
            state = stateForUi,
            onBack = { nav.popBackStack() },
            onEditClick = { /* optional: could navigate to HealthHeight for editing */ },
            onEditConfirm = { /* optional: post-edit action */ }
        )
    }
}
