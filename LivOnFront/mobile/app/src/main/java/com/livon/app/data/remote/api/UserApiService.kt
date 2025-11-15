package com.livon.app.data.remote.api

import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.POST

// Response DTOs matching backend spec
data class MyInfoResponse(
    val nickname: String,
    val profileImage: String?,
    val organizations: String?,
    val gender: String?,
    val birthdate: String?,
    // change: use a Map to represent arbitrary JSON object returned for healthSurvey
    // this avoids Moshi reflective serialization warnings and lets repository parse safely
    val healthSurvey: Map<String, Any?>?
)

// HealthSurvey fields (used for reference parsing in repository)
data class HealthSurveyResponse(
    val weight: Double?,
    val height: Double?,
    val steps: Int?,
    val sleepTime: Double?,
    val disease: String?,
    val sleepQuality: String?,
    val medicationsInfo: String?,
    val painArea: String?,
    val stressLevel: String?,
    val smokingStatus: String?,
    val avgSleepHours: Double?,
    val activityLevel: String?,
    val caffeineIntakeLevel: String?
)

// Request DTO for profile image update
data class ProfileImageRequest(val profileImageUrl: String)

// Request DTO for posting health survey to backend
data class HealthSurveyRequest(
    val steps: Int = 0,
    val sleepTime: Int = 0,
    val disease: String? = null,
    val sleepQuality: String? = null,
    val medicationsInfo: String? = null,
    val painArea: String? = null,
    val stressLevel: String? = null,
    val smokingStatus: String? = null,
    val avgSleepHours: Int = 0,
    val activityLevel: String? = null,
    val caffeineIntakeLevel: String? = null,
    val height: Int = 0,
    val weight: Int = 0
)

// Response DTO for AI health summary
data class HealthSummaryResponse(
    val userId: String,
    val summary: String
)

interface UserApiService {
    @GET("user/my-info")
    suspend fun getMyInfo(): ApiResponse<MyInfoResponse>

    @PUT("user/profileImage")
    suspend fun updateProfileImage(@Body req: ProfileImageRequest): ApiResponse<Any?>

    // POST health survey
    @POST("user/health-survey")
    suspend fun postHealthSurvey(@Body req: HealthSurveyRequest): ApiResponse<Any?>

    // POST AI health summary
    @POST("ai/health-summary")
    suspend fun postHealthSummary(): ApiResponse<HealthSummaryResponse>
}
