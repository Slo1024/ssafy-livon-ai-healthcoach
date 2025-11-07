package com.livon.app.data.remote.api

import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Body

// Response DTOs matching backend spec
data class MyInfoResponse(
    val nickname: String,
    val profileImage: String?,
    val organizations: String?,
    val gender: String?,
    val birthdate: String?,
    val healthSurvey: Any? // can be an object or an empty list, so keep generic and parse safely
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

interface UserApiService {
    @GET("/user/my-info")
    suspend fun getMyInfo(): ApiResponse<MyInfoResponse>

    @PUT("/user/profileImage")
    suspend fun updateProfileImage(@Body req: ProfileImageRequest): ApiResponse<Any?>
}
