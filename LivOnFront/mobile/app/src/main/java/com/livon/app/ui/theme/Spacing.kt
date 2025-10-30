package com.livon.app.ui.theme

import androidx.compose.ui.unit.dp

object Spacing {
    // 공통 패딩
    val Horizontal = 20.dp
    val BottomMargin = 24.dp
    val TopMargin = 24.dp


    // 그룹 A (기존 TopBar 사용 화면)
    // - 소셜로그인, 이용약관, 코치/회원 선택
    // - 이메일 로그인, 이메일/비번/닉네임 설정, 이메일 인증
    // - 일반/기업 선택, 회사 선택, 프로필 설정
    val TopbarToTitle_A = 80.dp   // TopBar 아래 → 요구사항 텍스트까지 80
    val TitleToDesc = 20.dp       // 요구사항↔캡션 간격 20
    val DescToContent = 20.dp
    val FieldBetween = 12.dp

    // 그룹 B (TopBar2 사용 화면)
    // - 성별/생년월일 (TopBar2 아래 → 요구사항 텍스트까지 30)
    // - 건강정보/생활습관 화면은 각 화면에서 별도 간격 처리
    val TopbarToTitle_B = 30.dp
}
