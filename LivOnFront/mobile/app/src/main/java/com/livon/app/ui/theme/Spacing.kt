package com.livon.app.ui.theme

import androidx.compose.ui.unit.dp

object Spacing {
    // 공통 패딩
    val Horizontal = 20.dp
    val BottomMargin = 24.dp

    // 그룹 A (기존 TopBar 사용 화면)
    // - 소셜로그인, 이용약관, 코치/회원 선택
    // - 이메일 로그인, 이메일/비번/닉네임 설정, 이메일 인증
    // - 일반/기업 선택, 회사 선택, 프로필 설정
    val TopbarToTitle_A = 70.dp   // ★ 요구사항 텍스트까지 70
    val TitleToDesc = 8.dp
    val DescToContent = 20.dp
    val FieldBetween = 12.dp

    // 그룹 B (TopBar2 사용 화면)
    // - 성별/생년월일
    // - 건강정보/생활습관 전 화면
    val TopbarToTitle_B = 30.dp   // ★ 요구사항 텍스트까지 30
}
