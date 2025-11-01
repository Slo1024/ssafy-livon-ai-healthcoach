package com.livon.app.data.repository

import com.livon.app.domain.model.Upcoming
import com.livon.app.domain.repository.HomeRepository
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class DummyHomeRepository : HomeRepository {
    override suspend fun fetchUpcoming(limit: Int): List<Upcoming> {
        delay(300) // 네트워크 흉내
        return listOf(
            Upcoming(UUID.randomUUID().toString(), "필라테스 클래스", LocalDate.now().plusDays(1), LocalTime.of(16, 40)),
            Upcoming(UUID.randomUUID().toString(), "식단 클래스",   LocalDate.now().plusDays(2), LocalTime.of(11, 20)),
        )
    }
}
