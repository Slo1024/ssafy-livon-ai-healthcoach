package com.livon.app.feature.member.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livon.app.R
import com.livon.app.feature.member.home.vm.MemberHomeUiState
import com.livon.app.feature.member.home.vm.MemberHomeViewModel

@Composable
fun MemberHomeRoute(
    onTapBooking: () -> Unit,
    onTapReservations: () -> Unit,
    vm: MemberHomeViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    MemberHomeScreen(
        state = state,
        onRefresh = { vm.load() },
        onTapBooking = onTapBooking,
        onTapReservations = onTapReservations
    )
}

@Composable
fun MemberHomeScreen(
    state: MemberHomeUiState,
    onRefresh: () -> Unit,
    onTapBooking: () -> Unit,
    onTapReservations: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 상단 심플 타이틀
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LIVON", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = onRefresh) { Text("다시 시도") }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {

                    // 환영 섹션
                    item {
                        Column {
                            Text("김싸피님", style = MaterialTheme.typography.titleLarge)
                            Text("환영합니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // 예약 섹션 헤더
                    item { Divider() }
                    item { Text("상담 / 코칭 예약", style = MaterialTheme.typography.titleLarge) }

                    // 예약하기/예약현황 카드 2개 (인라인 구현)
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 예약 하기 카드
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(MaterialTheme.shapes.large)
                                    .clickable { onTapBooking() },
                                shape = MaterialTheme.shapes.large,
                                tonalElevation = 2.dp
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("예약 하기", style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        "원하는 시간과 코치를 선택",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_back),
                                            contentDescription = null,
                                            modifier = Modifier.graphicsLayer { scaleX = -1f }, // 좌우반전
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // 예약 현황 카드
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(MaterialTheme.shapes.large)
                                    .clickable { onTapReservations() },
                                shape = MaterialTheme.shapes.large,
                                tonalElevation = 2.dp
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("예약 현황", style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        "예약/상담 스케줄 확인",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_back),
                                            contentDescription = null,
                                            modifier = Modifier.graphicsLayer { scaleX = -1f },
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 다가오는 상담 섹션
                    item { Divider() }
                    item { Text("다가오는 상담 / 코칭", style = MaterialTheme.typography.titleLarge) }

                    // 리스트 아이템 (인라인 구현)
                    items(state.upcoming, key = { it.id }) { u ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(u.title, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${u.date}  ${u.time}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = null,
                                modifier = Modifier.graphicsLayer { scaleX = -1f },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
