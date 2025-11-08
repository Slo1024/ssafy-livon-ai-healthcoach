package com.livon.app.domain.repository

import android.util.Log
import com.livon.app.BuildConfig
import com.livon.app.data.remote.api.GroupConsultationApiService
import com.livon.app.feature.member.reservation.ui.SampleClassInfo
import java.time.LocalDate

class GroupConsultationRepository(private val api: GroupConsultationApiService) {

    // DEV: temporary mock data to use when server has no classes (useful for local/dev testing).
    // Remove or guard behind more robust feature-flag before production.
    private fun devMockClasses(): List<SampleClassInfo> = listOf(
        SampleClassInfo(
            id = "dev-1",
            // Use realistic dev coach UUIDs (matches dev coaches returned by server in logs)
            coachId = "0cecda8a-2065-4551-a0cd-b3b5b6ae4685",
            date = LocalDate.now().plusDays(1),
            time = "09:00 ~ 10:00",
            type = "그룹 클래스",
            imageUrl = null,
            className = "데드리프트 기본 (DEV)",
            coachName = "김명주 DEV",
            description = "데모용 클래스 - 개발에서만 보입니다.",
            currentParticipants = 0,
            maxParticipants = 20
        ),
        SampleClassInfo(
            id = "dev-2",
            coachId = "dbfd8b56-189a-4055-87b0-913830a1741c",
            date = LocalDate.now().plusDays(2),
            time = "15:00 ~ 16:00",
            type = "그룹 클래스",
            imageUrl = null,
            className = "식단의 정석 (DEV)",
            coachName = "차민규 DEV",
            description = "데모용 클래스 - 개발에서만 보입니다.",
            currentParticipants = 3,
            maxParticipants = 25
        )
    )

    suspend fun fetchClasses(): Result<List<SampleClassInfo>> {
        return try {
            val res = api.findClasses()
            if (res.isSuccess && res.result != null) {
                val items = res.result.items.map { dto ->
                    // parse startAt date as LocalDate if possible
                    val date = try { LocalDate.parse(dto.startAt?.substring(0,10)) } catch (t: Throwable) { LocalDate.now() }
                    SampleClassInfo(
                        id = dto.id.toString(),
                        // NOTE: class list API does not include coach UUID in sample; avoid assigning coachName to coachId
                        coachId = "",
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

                // If server returned empty list, provide dev fallback when running debug build
                return if (items.isEmpty() && BuildConfig.DEBUG) {
                    Log.d("GroupConsultRepo", "server returned empty list -> using dev mock fallback")
                    Result.success(devMockClasses())
                } else {
                    Result.success(items)
                }
            } else {
                // API returned failure; in debug builds provide dev mock so UI can be tested on device.
                if (BuildConfig.DEBUG) {
                    Log.d("GroupConsultRepo", "api failure or empty result object -> using dev mock fallback (DEBUG)")
                    Result.success(devMockClasses())
                }
                return Result.failure(Exception(res.message ?: "failed to fetch classes"))
            }
        } catch (t: Throwable) {
            Log.d("GroupConsultRepo", "exception: ${t.message}")
            // On network/serialization errors, allow dev fallback on debug to keep UI testable
            if (BuildConfig.DEBUG) {
                Log.d("GroupConsultRepo", "exception occurred -> returning dev mock fallback (DEBUG)")
                return Result.success(devMockClasses())
            }
            Result.failure(t)
        }
    }

    suspend fun fetchClassDetail(id: String): Result<SampleClassInfo> {
        // Short-circuit for dev mock ids when running in DEBUG to avoid calling server with non-numeric ids
        if (BuildConfig.DEBUG && id.startsWith("dev-")) {
            val match = devMockClasses().find { it.id == id }
            if (match != null) {
                Log.d("GroupConsultRepo", "returning dev mock detail for id=$id (DEBUG)")
                return Result.success(match)
            }
        }

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
                Result.success(item)
            } else {
                if (BuildConfig.DEBUG) {
                    // If detail not found or API failed, try to return matching dev mock detail for quick testing
                    Log.d("GroupConsultRepo", "detail API empty/fail -> trying to return matching dev mock detail (DEBUG)")
                    val match = devMockClasses().find { it.id == id }
                    if (match != null) return Result.success(match)
                    return Result.success(devMockClasses().first())
                }
                Result.failure(Exception(res.message ?: "failed to fetch class detail"))
            }
        } catch (t: Throwable) {
            // On error, allow debug fallback
            Log.d("GroupConsultRepo", "fetchClassDetail exception: ${t.message}")
            if (BuildConfig.DEBUG) {
                val match = devMockClasses().find { it.id == id }
                if (match != null) return Result.success(match)
                return Result.success(devMockClasses().first())
            }
            Result.failure(t)
        }
    }
}
