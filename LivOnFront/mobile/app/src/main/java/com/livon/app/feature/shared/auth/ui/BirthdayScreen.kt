// com/livon/app/feature/shared/auth/ui/BirthdayScreen.kt
package com.livon.app.feature.shared.auth.ui

import android.util.Log
import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface
import java.time.LocalDate
import java.time.YearMonth
import com.livon.app.ui.theme.Gray
import com.livon.app.ui.theme.Gray2
import com.livon.app.ui.theme.Main

@Composable
fun BirthdayScreen(
    onBack: () -> Unit = {},
    onNext: (year: Int, month: Int, day: Int) -> Unit = { _,_,_ -> }
) {
    // 기본값: 오늘 날짜
    val today = remember { LocalDate.now() }
    var year by remember { mutableStateOf(today.year) }
    var month by remember { mutableStateOf(today.monthValue) }
    var day by remember { mutableStateOf(today.dayOfMonth) }

    // 일수는 연/월에 따라 변함
    val daysInMonth = remember(year, month) {
        YearMonth.of(year, month).lengthOfMonth()
    }
    if (day > daysInMonth) day = daysInMonth

    val isEnabled = true // 퍼블리싱 단계에선 항상 가능. 필요시 최소 연령 조건 등 추가

    CommonSignUpScreenB(
        title = "건강 정보 입력",
        onBack = onBack,
        bottomBar = {
            PrimaryButtonBottom(
                text = "다음",
                enabled = isEnabled,
                onClick = {
                    Log.d("BirthdayScreen", "Next clicked: y=$year m=$month d=$day")
                    onNext(year, month, day)
                }
            )
        }
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "생년월일을 입력해주세요.",
            style = MaterialTheme.typography.titleLarge.copy(color = Gray2)
        )

        Spacer(Modifier.height(150.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 연도
            PickerColumn(
                value = year,
                onValueChange = { year = it },
                min = 1900,
                max = today.year,
                formatter = { "$it" }
            )
            Spacer(Modifier.width(16.dp))

            // 월
            PickerColumn(
                value = month,
                onValueChange = { month = it },
                min = 1,
                max = 12,
                formatter = { "${it}월" }
            )
            Spacer(Modifier.width(16.dp))

            // 일
            PickerColumn(
                value = day,
                onValueChange = { day = it },
                min = 1,
                max = daysInMonth,
                formatter = { "$it" }
            )
        }

        // 아래 공간 확보(디자인 여유)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PickerColumn(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    formatter: (Int) -> String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current

        // NumberPicker 자체
        AndroidView(
            modifier = Modifier
                .width(90.dp)
                .height(140.dp),
            factory = {
                NumberPicker(context).apply {
                    minValue = min
                    maxValue = max
                    this.value = value        // <-- 수신자 명시로 충돌 해소
                    wrapSelectorWheel = true
                    setFormatter { v -> formatter(v) }
                }
            },
            update = { picker ->
                if (picker.value != value) picker.value = value
                picker.setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }
                picker.minValue = min
                picker.maxValue = max
            }
        )

        // 선택선(밑줄) – 피그마 느낌
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .width(70.dp)
                .height(2.dp)
        ) {
            // 연한 가이드 라인
            Box(
                Modifier
                    .matchParentSize()
                    .padding(horizontal = 4.dp)
                    .height(1.dp)
            )
            // 메인 색상 밑줄
            Box(
                Modifier
                    .matchParentSize()
                    .height(2.dp)
                    .padding(horizontal = 4.dp)
                    .align(Alignment.Center)
                    .background(Main)
            )
        }
    }
}

/* ---------------- Preview ---------------- */

@Preview(showBackground = true)
@Composable
private fun PreviewBirthdayScreen() = PreviewSurface { BirthdayScreen() }

