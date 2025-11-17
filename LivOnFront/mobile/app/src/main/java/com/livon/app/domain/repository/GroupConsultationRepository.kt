package com.livon.app.domain.repository

import android.util.Log
import com.livon.app.data.remote.api.GroupConsultationApiService
import com.livon.app.feature.member.reservation.ui.SampleClassInfo
import java.time.LocalDate

class GroupConsultationRepository(private val api: GroupConsultationApiService) {

    suspend fun fetchClasses(): Result<List<SampleClassInfo>> {
        try {
            val res = api.findClasses()
            if (res.isSuccess && res.result != null) {
                val items = res.result.items.map { dto ->
                    // parse startAt date as LocalDate if possible
                    val date = try { LocalDate.parse(dto.startAt?.substring(0,10)) } catch (t: Throwable) { LocalDate.now() }
                    SampleClassInfo(
                        id = dto.id.toString(),
                        // [수정] API 응답에 포함된 coachId 사용
                        coachId = dto.coachId ?: "",
                        date = date,
                        time = dto.startAt?.let { s ->
                            try {
                                val start = s.substring(11,16)
                                val end = dto.endAt?.substring(11,16) ?: ""
                                "$start ~ $end"
                            } catch (_: Exception) { "" }
                        } ?: "",
                        type = "그룹 클래스",
                        imageUrl = dto.imageUrl,
                        className = dto.title,
                        coachName = dto.coachName ?: "",
                        description = "",
                        currentParticipants = dto.currentParticipants,
                        maxParticipants = dto.capacity
                    )
                }

                // Return server-provided list (may be empty).
                return Result.success(items)
            } else {
                // API returned failure or null result. Do not substitute dev mock data here.
                return Result.failure(Exception(res.message ?: "failed to fetch classes"))
            }
        } catch (t: Throwable) {
            Log.d("GroupConsultRepo", "exception: ${t.message}")
            // Propagate the failure so calling code sees actual error.
            return Result.failure(t)
        }
    }

    suspend fun fetchClassDetail(id: String): Result<SampleClassInfo> {
        // Always attempt real API lookup for class detail
        try {
            val res = api.findClassDetail(id)
            if (res.isSuccess && res.result != null) {
                val dto = res.result
                val date = try { LocalDate.parse(dto.startAt?.substring(0,10)) } catch (t: Throwable) { LocalDate.now() }
                val item = SampleClassInfo(
                    id = dto.id.toString(),
                    coachId = dto.coach?.id ?: "",
                    date = date,
                    time = dto.startAt?.let { s ->
                        try {
                            val start = s.substring(11,16)
                            val end = dto.endAt?.substring(11,16) ?: ""
                            "$start ~ $end"
                        } catch (_: Exception) { "" }
                    } ?: "",
                    type = "그룹 클래스",
                    imageUrl = dto.imageUrl,
                    className = dto.title,
                    coachName = dto.coach?.nickname ?: "",
                    description = dto.description ?: "",
                    currentParticipants = dto.currentParticipants,
                    maxParticipants = dto.capacity
                )
                return Result.success(item)
            } else {
                // Do not return dev mock details; surface the failure instead
                return Result.failure(Exception(res.message ?: "failed to fetch class detail"))
            }
        } catch (t: Throwable) {
            Log.d("GroupConsultRepo", "fetchClassDetail exception: ${t.message}")
            return Result.failure(t)
        }
    }
}
