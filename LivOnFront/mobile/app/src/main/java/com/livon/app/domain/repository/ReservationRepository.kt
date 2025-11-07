package com.livon.app.domain.repository

import com.livon.app.data.remote.api.ReservationApiService
import com.livon.app.feature.member.reservation.ui.ReservationUi
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ReservationRepository(private val api: ReservationApiService) {
    suspend fun fetchUpcoming(): Result<List<ReservationUi>> {
        return try {
            val dtos = api.getUpcoming()
            val list = dtos.map { dto ->
                // parse date string if possible
                val date = try { LocalDate.parse(dto.date) } catch (t: Throwable) { LocalDate.now() }
                val timeText = dto.timeText
                ReservationUi(
                    id = dto.id,
                    date = date,
                    className = dto.className,
                    coachName = dto.coachName,
                    coachRole = dto.coachRole,
                    coachIntro = dto.coachIntro,
                    timeText = timeText,
                    classIntro = dto.classIntro,
                    imageResId = dto.imageResId,
                    isLive = dto.isLive,
                    sessionTypeLabel = dto.sessionTypeLabel,
                    hasAiReport = dto.hasAiReport
                )
            }
            Result.success(list)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}

