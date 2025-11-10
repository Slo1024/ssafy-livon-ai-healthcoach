package com.livon.app.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path

// DTOs for group consultations (list + detail)
data class GroupConsultationListItemDto(
    val id: Long,
    val title: String,
    val imageUrl: String?,
    val startAt: String?,
    val endAt: String?,
    val capacity: Int,
    val currentParticipants: Int,
    val availableSeats: Int,
    val isFull: Boolean,
    val coachName: String?,
    val coachProfileImage: String?
)

data class PaginatedGroupConsultationListResult(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<GroupConsultationListItemDto>
)

data class GroupConsultationDetailCoachDto(
    val id: String,
    val nickname: String,
    val profileImage: String?,
    val job: String?,
    val introduce: String?
)

data class GroupConsultationDetailDto(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val startAt: String?,
    val endAt: String?,
    val capacity: Int,
    val currentParticipants: Int,
    val availableSeats: Int,
    val isFull: Boolean,
    val coach: GroupConsultationDetailCoachDto?
)

interface GroupConsultationApiService {
    @GET("group-consultations")
    suspend fun findClasses(): ApiResponse<PaginatedGroupConsultationListResult>

    @GET("group-consultations/{id}")
    suspend fun findClassDetail(@Path("id") id: String): ApiResponse<GroupConsultationDetailDto>
}
