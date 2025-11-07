package com.livon.app.domain.repository

import android.util.Log
import com.livon.app.data.remote.api.CoachApiService
import com.livon.app.feature.member.reservation.model.CoachUIModel

class CoachRepository(private val api: CoachApiService) {
    suspend fun fetchCoaches(): Result<List<CoachUIModel>> {
        return try {
            val res = api.findCoaches()
            Log.d("CoachRepository", "findCoaches: success=${res.isSuccess}")
            if (res.isSuccess && res.result != null) {
                val list = res.result.items.map { item ->
                    CoachUIModel(
                        id = item.userId,
                        name = item.nickname,
                        job = item.job ?: "",
                        intro = item.introduce ?: "",
                        avatarUrl = item.profileImage,
                        certificates = item.certificates ?: emptyList(),
                        isCorporate = false // backend doesn't return isCorporate; set false by default
                    )
                }
                Result.success(list)
            } else Result.failure(Exception(res.message ?: "fetch coaches failed"))
        } catch (t: Throwable) {
            Log.d("CoachRepository", "exception: ${t.message}")
            Result.failure(t)
        }
    }

    suspend fun fetchCoachDetail(coachId: String): Result<CoachUIModel> {
        return try {
            val res = api.findCoachDetail(coachId)
            if (res.isSuccess && res.result != null) {
                val item = res.result
                val model = CoachUIModel(
                    id = item.userId,
                    name = item.nickname,
                    job = item.job ?: "",
                    intro = item.introduce ?: "",
                    avatarUrl = item.profileImage,
                    certificates = item.certificates ?: emptyList(),
                    isCorporate = false
                )
                Result.success(model)
            } else Result.failure(Exception(res.message ?: "fetch coach detail failed"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
