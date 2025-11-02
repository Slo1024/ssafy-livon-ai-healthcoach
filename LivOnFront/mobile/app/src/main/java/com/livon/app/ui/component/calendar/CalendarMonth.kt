package com.livon.app.ui.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

@Composable
fun MonthNavigator(
    yearMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("〈", modifier = Modifier.clickable { onPrev() })
        Text("${yearMonth.monthValue}월", style = MaterialTheme.typography.titleMedium)
        Text("〉", modifier = Modifier.clickable { onNext() })
    }
}

@Composable
fun CalendarMonth(
    yearMonth: YearMonth,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("일","월","화","수","목","금","토").forEach {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))

        val firstDay = yearMonth.atDay(1)
        val startOffset = (firstDay.dayOfWeek.value % 7) // 일요일=0
        val days = yearMonth.lengthOfMonth()
        val cells = startOffset + days
        val rows = ceil(cells / 7f).toInt()

        var day = 1
        repeat(rows) { r ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(7) { c ->
                    val index = r * 7 + c
                    if (index < startOffset || day > days) {
                        Spacer(Modifier.weight(1f).height(40.dp))
                    } else {
                        val date = yearMonth.atDay(day++)
                        val isSelected = date == selected
                        Box(
                            Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(40.dp))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                                    else Color.Transparent
                                )
                                .clickable { onSelect(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${date.dayOfMonth}",
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCalendarMonth() {
    val ym = YearMonth.now()
    CalendarMonth(
        yearMonth = ym,
        selected = ym.atDay(15),
        onSelect = {}
    )
}
