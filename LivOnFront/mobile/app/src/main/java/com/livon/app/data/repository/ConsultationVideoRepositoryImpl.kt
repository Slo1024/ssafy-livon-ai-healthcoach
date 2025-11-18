package com.livon.app.data.repository

import android.util.Log
import com.livon.app.data.remote.api.ConsultationVideoApi
import com.livon.app.data.remote.api.ConsultationVideoApiImpl
import com.livon.app.domain.repository.ConsultationVideoRepository

class ConsultationVideoRepositoryImpl(
    private val api: ConsultationVideoApi = ConsultationVideoApiImpl()
) : ConsultationVideoRepository {

    override suspend fun getSummary(consultationId: Long): Result<String> {
        return try {
            Log.d("ConsultationVideoRepo", "getSummary: calling API for consultationId=$consultationId")
            Log.d("ConsultationVideoRepo", "getSummary: API endpoint should be GET /api/v1/gcp/video-summary/$consultationId")
            val response = api.getSummary(consultationId)
            
            if (response.isSuccess && response.result != null) {
                Log.d("ConsultationVideoRepo", "getSummary: success for consultationId=$consultationId, summary length=${response.result.summary.length}")
                Result.success(response.result.summary)
            } else {
                val errorMsg = response.message ?: "영상 요약을 불러올 수 없습니다."
                Log.w("ConsultationVideoRepo", "getSummary: API returned failure for consultationId=$consultationId: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ConsultationVideoRepo", "getSummary: exception for consultationId=$consultationId", e)
            Log.e("ConsultationVideoRepo", "getSummary: exception type=${e.javaClass.simpleName}, message=${e.message}")
            if (e is retrofit2.HttpException) {
                Log.e("ConsultationVideoRepo", "getSummary: HTTP ${e.code()}, response=${e.response()?.errorBody()?.string()}")
            }
            Result.failure(e)
        }
    }
}

