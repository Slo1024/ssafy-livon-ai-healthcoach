// com/livon/app/navigation/MemberNavGraph.kt
package com.livon.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.livon.app.feature.member.home.ui.MemberHomeRoute
import com.livon.app.feature.member.reservation.ui.*
import com.livon.app.feature.member.reservation.model.CoachUIModel
import com.livon.app.feature.member.my.MyInfoUiState
import com.livon.app.feature.member.my.MyInfoScreen
import com.livon.app.feature.member.my.MyPageScreen
import com.livon.app.feature.shared.auth.ui.ReservationModeSelectScreen
import java.net.URLDecoder
import java.time.LocalDate

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

    // Reusable mock coaches for dev/debug: put at top so multiple routes can use them
    val devMockCoaches = listOf(
        CoachUIModel(id = "1", name = "김도윤", job = "피트니스 코치", intro = "체형 분석 기반 근력·유산소 균형 프로그램", avatarUrl = null, isCorporate = false),
        CoachUIModel(id = "2", name = "박지성", job = "유산소 트레이너", intro = "유산소 퍼포먼스 향상 및 빌드업 계획", avatarUrl = null, isCorporate = true),
        CoachUIModel(id = "3", name = "손흥민", job = "러닝 코치", intro = "러닝 기술, 착지 개선, 인터벌 프로그램 설계", avatarUrl = null, isCorporate = false),
        CoachUIModel(id = "4", name = "이강인", job = "필라테스", intro = "코어 강화, 밸런싱 중심 프로그램", avatarUrl = null, isCorporate = true),
        CoachUIModel(id = "5", name = "정우영", job = "영양 코치", intro = "체형·목표에 맞는 식단 설계 및 점검", avatarUrl = null, isCorporate = false),
        CoachUIModel(id = "6", name = "황희찬", job = "근력 트레이너", intro = "파워 및 근성 향상 집중 트레이닝", avatarUrl = null, isCorporate = false),
        CoachUIModel(id = "7", name = "김민재", job = "바디 리셋", intro = "스트레칭→근력 밸런싱 리커버리 플랜", avatarUrl = null, isCorporate = true),
        CoachUIModel(id = "8", name = "조규성", job = "피트니스", intro = "근력·유연성 균형 맞춤 루틴", avatarUrl = null, isCorporate = false),
        CoachUIModel(id = "9", name = "백승호", job = "필라테스", intro = "골반·척추 정렬 중심 루틴", avatarUrl = null, isCorporate = false),
        CoachUIModel(id = "10", name = "이승우", job = "PT 코치", intro = "1:1 자세 교정 집중 코칭", avatarUrl = null, isCorporate = false),
        CoachUIModel(id = "11", name = "권창훈", job = "영양", intro = "식단 전략 설계·실행 지속케어", avatarUrl = null, isCorporate = true),
        CoachUIModel(id = "12", name = "안정환", job = "피트니스", intro = "부상 예방 중심 트레이닝", avatarUrl = null, isCorporate = false)
    )

    val useDevMocks = isDebugBuild()

    composable("member_home") {
        MemberHomeRoute(
            onTapBooking = { nav.navigate("reservation_model_select") },
            onTapReservations = { nav.navigate("reservations") },
            onTapMyPage = { nav.navigate("mypage") }
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
        ReservationModeSelectScreen(onComplete = { mode ->
            if (mode == "personal") nav.navigate("coach_list") else nav.navigate("class_reservation")
        })
    }

    composable("coach_list") {
        val coachesToShow = if (useDevMocks) devMockCoaches else emptyList()
        val isCorporate = useDevMocks
        val loadMore = useDevMocks

        CoachListScreen(
            coaches = coachesToShow,
            onBack = { nav.popBackStack() },
            modifier = androidx.compose.ui.Modifier,
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
        val coach = if (useDevMocks) devMockCoaches.find { it.id == coachId } else null
        val showSchedule = mode == "personal"
        CoachDetailScreenNav(nav = nav, coach = coach, showSchedule = showSchedule)
    }

    // New navigable route that accepts coachName and date as path params (date: ISO yyyy-MM-dd)
    composable("qna_submit/{coachName}/{date}") { backStackEntry ->
        val encodedName = backStackEntry.arguments?.getString("coachName") ?: ""
        val decodedName = try { URLDecoder.decode(encodedName, "UTF-8") } catch (t: Throwable) { encodedName }
        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
        val parsedDate = try { LocalDate.parse(dateStr) } catch (t: Throwable) { LocalDate.now() }

        QnASubmitScreen(
            coachName = decodedName,
            selectedDate = parsedDate,
            onBack = { nav.popBackStack() },
            onConfirmReservation = { _ -> nav.navigate("reservations") },
            onNavigateHome = { nav.navigate("member_home") },
            onNavigateToMyHealthInfo = { /* noop */ },
            navController = nav
        )
    }

    // Keep the old qna_submit fallback for previews/tests
    composable("qna_submit") {
        QnASubmitScreen(
            coachName = "코치",
            selectedDate = LocalDate.now(),
            onBack = { nav.popBackStack() },
            onConfirmReservation = { _ -> nav.navigate("reservations") },
            onNavigateHome = { nav.navigate("member_home") },
            onNavigateToMyHealthInfo = { /* noop */ },
            navController = nav
        )
    }

    composable("class_reservation") {
        // Provide mock classes in dev mode so emulator shows content like preview
        val mockClasses = listOf(
            SampleClassInfo(
                id = "1",
                coachId = "c1",
                date = LocalDate.now(),
                time = "11:00 ~ 12:00",
                type = "일반 클래스",
                imageUrl = null,
                className = "직장인을 위한 코어 강화",
                coachName = "김리본 코치",
                description = "점심시간 30분 집중 코어 운동.",
                currentParticipants = 7,
                maxParticipants = 10
            ),
            SampleClassInfo(
                id = "2",
                coachId = "c2",
                date = LocalDate.now().plusDays(1),
                time = "19:00 ~ 20:00",
                type = "기업 클래스",
                imageUrl = null,
                className = "퇴근 후 스트레칭",
                coachName = "박생존 코치",
                description = "힐링 스트레칭.",
                currentParticipants = 10,
                maxParticipants = 10
            ),
            SampleClassInfo(
                id = "3",
                coachId = "c3",
                date = LocalDate.now().plusDays(2),
                time = "09:00 ~ 10:00",
                type = "일반 클래스",
                imageUrl = null,
                className = "모닝 요가",
                coachName = "이영희 코치",
                description = "하루를 상쾌하게 시작하는 요가 클래스",
                currentParticipants = 5,
                maxParticipants = 12
            ),
            SampleClassInfo(
                id = "4",
                coachId = "c4",
                date = LocalDate.now().plusDays(3),
                time = "12:30 ~ 13:10",
                type = "단기 클래스",
                imageUrl = null,
                className = "점심 필라테스",
                coachName = "최민수 코치",
                description = "점심시간 짧고 굵게 스트레칭",
                currentParticipants = 8,
                maxParticipants = 15
            ),
            SampleClassInfo(
                id = "5",
                coachId = "c5",
                date = LocalDate.now().plusDays(4),
                time = "18:00 ~ 19:00",
                type = "그룹 클래스",
                imageUrl = null,
                className = "저녁 힐링 스트레칭",
                coachName = "한지은 코치",
                description = "하루 피로를 풀어주는 스트레칭",
                currentParticipants = 6,
                maxParticipants = 12
            ),
            SampleClassInfo(
                id = "6",
                coachId = "c6",
                date = LocalDate.now().plusDays(5),
                time = "20:00 ~ 21:00",
                type = "심화 클래스",
                imageUrl = null,
                className = "심화 근력 트레이닝",
                coachName = "박태환 코치",
                description = "근력과 코어를 동시에 강화하는 심화 수업",
                currentParticipants = 3,
                maxParticipants = 8
            ),
            SampleClassInfo(
                id = "7",
                coachId = "c7",
                date = LocalDate.now().plusDays(6),
                time = "07:30 ~ 08:10",
                type = "아침 클래스",
                imageUrl = null,
                className = "모닝 런",
                coachName = "김철수 코치",
                description = "출근 전 가볍게 달리기",
                currentParticipants = 9,
                maxParticipants = 20
            ),
            SampleClassInfo(
                id = "8",
                coachId = "c8",
                date = LocalDate.now().plusDays(7),
                time = "15:00 ~ 16:00",
                type = "커뮤니티 클래스",
                imageUrl = null,
                className = "주말 필라테스",
                coachName = "오승환 코치",
                description = "주말에 즐기는 필라테스로 몸과 마음을 케어",
                currentParticipants = 12,
                maxParticipants = 12
            ),
            SampleClassInfo(
                id = "9",
                coachId = "c9",
                date = LocalDate.now().plusDays(8),
                time = "14:00 ~ 15:00",
                type = "특별 클래스",
                imageUrl = null,
                className = "체형 교정 클래스",
                coachName = "정하영 코치",
                description = "전문가의 체형 교정 가이드",
                currentParticipants = 2,
                maxParticipants = 6
            ),
            SampleClassInfo(
                id = "10",
                coachId = "c10",
                date = LocalDate.now().plusDays(9),
                time = "17:30 ~ 18:30",
                type = "그룹 클래스",
                imageUrl = null,
                className = "저녁 근력 회복",
                coachName = "이수진 코치",
                description = "저녁 시간대 근력 회복을 위한 클래스",
                currentParticipants = 4,
                maxParticipants = 10
            )
        )

        val classesToShow = if (useDevMocks) mockClasses else emptyList<SampleClassInfo>()

        ClassReservationScreen(
            classes = classesToShow,
            onCardClick = { /* noop or nav to class_detail with id */ },
            onCoachClick = { coachId -> nav.navigate("coach_detail/$coachId/group") }
        )
    }

    composable("class_detail") {
        ClassDetailScreen(
            className = "클래스",
            coachName = "코치",
            classInfo = "상세 정보",
            onBack = { nav.popBackStack() },
            onReserveClick = { /* noop */ },
            onNavigateHome = { nav.navigate("member_home") },
            onNavigateToMyPage = { /* noop */ },
            navController = nav
        )
    }

    composable("reservations") {
        // dev mock reservations
        val today = LocalDate.now()
        val devCurrent = listOf(
            ReservationUi(
                id = "r1",
                date = today.plusDays(1),
                className = "개인 상담",
                coachName = "김도윤",
                coachRole = "임상심리사",
                coachIntro = "마음 회복 전문",
                timeText = "오전 10:00 ~ 11:00",
                classIntro = "심리 상담 세션",
                imageResId = null,
                isLive = false
            ),
            ReservationUi(
                id = "r2",
                date = today,
                className = "필라테스",
                coachName = "박지성",
                coachRole = "트레이너",
                coachIntro = "유연성 전문가",
                timeText = "오후 3:00 ~ 4:00",
                classIntro = "체형 개선 그룹 레슨",
                imageResId = null,
                isLive = true
            )
        )

        val devPast = listOf(
            ReservationUi(
                id = "p1",
                date = today.minusDays(5),
                className = "개인 상담",
                coachName = "최코치",
                coachRole = "코치",
                coachIntro = "코어 전문가",
                timeText = "오전 9:00 ~ 10:00",
                classIntro = "개인 집중 코칭",
                imageResId = null,
                sessionTypeLabel = "개인 상담",
                hasAiReport = true
            ),
            ReservationUi(
                id = "p2",
                date = today.minusDays(10),
                className = "모닝 요가",
                coachName = "박코치",
                coachRole = "요가",
                coachIntro = "힐링 요가",
                timeText = "오전 8:00 ~ 9:00",
                classIntro = "편안한 요가",
                imageResId = null,
                sessionTypeLabel = "그룹 상담",
                hasAiReport = false
            )
        )

        ReservationStatusScreen(
            current = devCurrent,
            past = devPast,
            onBack = { nav.popBackStack() },
            onDetail = { item ->
                // navigate to reservation detail route with id
                nav.navigate("reservation_detail/${item.id}")
            },
            onCancel = { item -> /* TODO: cancel logic */ },
            onJoin = { item -> /* TODO: join logic */ },
            onAiAnalyze = { item ->
                // navigate to AI result screen
                nav.navigate("ai_result/${item.id}")
            }
        )
    }

    // reservation detail route: show ReservationDetailScreen based on id
    composable("reservation_detail/{resId}") { backStackEntry ->
        val resId = backStackEntry.arguments?.getString("resId") ?: ""
        // Find in dev lists (recreate same lists)
        val today = LocalDate.now()
        val all = listOf(
            ReservationUi(
                id = "r1",
                date = today.plusDays(1),
                className = "개인 상담",
                coachName = "김도윤",
                coachRole = "임상심리사",
                coachIntro = "마음 회복 전문",
                timeText = "오전 10:00 ~ 11:00",
                classIntro = "심리 상담 세션",
                imageResId = null,
                isLive = false
            ),
            ReservationUi(
                id = "r2",
                date = today,
                className = "필라테스",
                coachName = "박지성",
                coachRole = "트레이너",
                coachIntro = "유연성 전문가",
                timeText = "오후 3:00 ~ 4:00",
                classIntro = "체형 개선 그룹 레슨",
                imageResId = null,
                isLive = true
            ),
            ReservationUi(
                id = "p1",
                date = today.minusDays(5),
                className = "개인 상담",
                coachName = "최코치",
                coachRole = "코치",
                coachIntro = "코어 전문가",
                timeText = "오전 9:00 ~ 10:00",
                classIntro = "개인 집중 코칭",
                imageResId = null,
                sessionTypeLabel = "개인 상담",
                hasAiReport = true
            ),
            ReservationUi(
                id = "p2",
                date = today.minusDays(10),
                className = "모닝 요가",
                coachName = "박코치",
                coachRole = "요가",
                coachIntro = "힐링 요가",
                timeText = "오전 8:00 ~ 9:00",
                classIntro = "편안한 요가",
                imageResId = null,
                sessionTypeLabel = "그룹 상담",
                hasAiReport = false
            )
        )

        val item = all.find { it.id == resId } ?: all.first()
        val type = when {
            // current if date >= today
            item.date >= today -> ReservationDetailType.Current
            item.sessionTypeLabel == "개인 상담" -> ReservationDetailType.PastPersonal
            else -> ReservationDetailType.PastGroup
        }

        // create coach and session models
        val coachMini = CoachMini(
            name = item.coachName,
            title = item.coachRole,
            specialties = item.coachIntro,
            workplace = "연결된 직장"
        )
        val session = SessionInfo(
            dateText = "${item.date.monthValue}월 ${item.date.dayOfMonth}일",
            timeText = item.timeText,
            modelText = item.className,
            appliedText = null
        )

        ReservationDetailScreen(
            type = type,
            coach = coachMini,
            session = session,
            aiSummary = if (item.hasAiReport) "AI 분석 결과 예시입니다." else null,
            qnas = listOf("Q1 내용", "Q2 내용"),
            onBack = { nav.popBackStack() },
            onDelete = { /* TODO */ },
            // NOTE (dev): 현재는 개발 편의상 하드코딩된 코치 id(1)로 '코치 보기' 네비게이션을 연결해 두었습니다.
            // 실제 배포/연동 시에는 예약 데이터(item)에 포함된 실제 코치 id를 사용해야 합니다.
            // 예시 변경: onSeeCoach = { nav.navigate("coach_detail/${item.coachId}/personal") }
            // 필요하시면 제가 이 매핑을 자동으로 적용해 드리겠습니다.
            onSeeCoach = { nav.navigate("coach_detail/1/personal") },
            onSeeAiDetail = { nav.navigate("ai_result/${item.id}") }
        )
    }

    // AI result route
    composable("ai_result/{resId}") { backStackEntry ->
        val resId = backStackEntry.arguments?.getString("resId") ?: ""
        val today = LocalDate.now()
        val item = ReservationUi(
            id = resId,
            date = today.minusDays(5),
            className = "개인 상담",
            coachName = "최코치",
            coachRole = "코치",
            coachIntro = "코어 전문가",
            timeText = "오전 9:00 ~ 10:00",
            classIntro = "개인 집중 코칭",
            imageResId = null,
            sessionTypeLabel = "개인 상담",
            hasAiReport = true
        )

        com.livon.app.feature.member.schedule.ui.AiResultScreen(
            memberName = "김싸피",
            counselingDateText = "${item.date.monthValue}월 ${item.date.dayOfMonth}일",
            counselingName = item.className,
            aiSummary = "AI 분석 결과 예시 텍스트입니다.",
            onBack = { nav.popBackStack() }
        )
    }
}
