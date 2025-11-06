package com.livon.app.domain.repository

import android.net.Uri
import android.util.Log
import com.livon.app.data.remote.api.HealthSurveyResponse
import com.livon.app.data.remote.api.MyInfoResponse
import com.livon.app.data.remote.api.UserApiService
import com.livon.app.feature.member.my.MyInfoUiState
import com.squareup.moshi.Moshi
import com.livon.app.data.remote.api.ProfileImageRequest

class UserRepository(private val api: UserApiService) {
    private val moshi = Moshi.Builder().build()

    suspend fun getMyInfo(): Result<MyInfoUiState> {
        return try {
            val res = api.getMyInfo()
            Log.d("UserRepository", "getMyInfo: isSuccess=${res.isSuccess}, msg=${res.message}")
            if (res.isSuccess && res.result != null) {
                val r: MyInfoResponse = res.result

                // parse healthSurvey which can be empty list or an object
                var health: HealthSurveyResponse? = null
                try {
                    if (r.healthSurvey != null) {
                        val jsonAdapter = moshi.adapter(HealthSurveyResponse::class.java)
                        // r.healthSurvey may be Map or List depending on server; convert via toString JSON
                        val raw = moshi.adapter(Any::class.java).toJson(r.healthSurvey)
                        health = jsonAdapter.fromJson(raw)
                    }
                } catch (t: Throwable) {
                    Log.d("UserRepository", "health parse error: ${t.message}")
                }

                val state = MyInfoUiState(
                    nickname = r.nickname,
                    gender = r.gender,
                    birthday = r.birthdate,
                    profileImageUri = r.profileImage?.let { Uri.parse(it) },
                    heightCm = health?.height?.toString(),
                    weightKg = health?.weight?.toString(),
                    condition = health?.disease,
                    sleepQuality = health?.sleepQuality,
                    medication = health?.medicationsInfo,
                    painArea = health?.painArea,
                    stress = health?.stressLevel,
                    smoking = health?.smokingStatus,
                    alcohol = health?.activityLevel,
                    sleepHours = health?.sleepTime?.toString(),
                    activityLevel = health?.activityLevel,
                    caffeine = health?.caffeineIntakeLevel
                )

                Result.success(state)
            } else {
                Result.failure(Exception(res.message ?: "myinfo failed"))
            }
        } catch (t: Throwable) {
            Log.d("UserRepository", "exception: ${t.message}")
            Result.failure(t)
        }
    }

    suspend fun updateProfileImage(profileImageUrl: String): Result<Boolean> {
        return try {
            val req = ProfileImageRequest(profileImageUrl)
            val res = api.updateProfileImage(req)
            Log.d("UserRepository", "updateProfileImage: isSuccess=${res.isSuccess}, msg=${res.message}")
            if (res.isSuccess) Result.success(true) else Result.failure(Exception(res.message ?: "update failed"))
        } catch (t: Throwable) {
            Log.d("UserRepository", "updateProfileImage exception: ${t.message}")
            Result.failure(t)
        }
    }
}
