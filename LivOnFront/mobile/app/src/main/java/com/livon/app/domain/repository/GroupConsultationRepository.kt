package com.livon.app.domain.repository

import android.util.Log
import com.livon.app.data.remote.api.GroupConsultationApiService
import com.livon.app.feature.member.reservation.ui.SampleClassInfo
import java.time.LocalDate

class GroupConsultationRepository(private val api: GroupConsultationApiService) {
    suspend fun fetchClasses(): Result<List<SampleClassInfo>> {
        return try {
            val res = api.findClasses()
            if (res.isSuccess && res.result != null) {
                val items = res.result.items.map { dto ->
                    // parse startAt date as LocalDate if possible
                    val date = try { LocalDate.parse(dto.startAt?.substring(0,10)) } catch (t: Throwable) { LocalDate.now() }
                    SampleClassInfo(
                        id = dto.id.toString(),
                        coachId = dto.coachName ?: "",
                        date = date,
                        time = dto.startAt?.let { s ->
                            try {
                                val start = s.substring(11,16)
                                val end = dto.endAt?.substring(11,16) ?: ""
                                "$start ~ $end"
                            } catch (_: Exception) { dto.startAt ?: "" }
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
                Result.success(items)
            } else {
                Result.failure(Exception(res.message ?: "failed to fetch classes"))
            }
        } catch (t: Throwable) {
            Log.d("GroupConsultRepo", "exception: ${t.message}")
            Result.failure(t)
        }
    }

    suspend fun fetchClassDetail(id: String): Result<SampleClassInfo> {
        return try {
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
                        } catch (_: Exception) { dto.startAt ?: "" }
                    } ?: "",
                    type = "그룹 클래스",
                    imageUrl = dto.imageUrl,
                    className = dto.title,
                    coachName = dto.coach?.nickname ?: "",
                    description = dto.description ?: "",
                    currentParticipants = dto.currentParticipants,
                    maxParticipants = dto.capacity
                )
                Result.success(item)
            } else Result.failure(Exception(res.message ?: "failed to fetch class detail"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
