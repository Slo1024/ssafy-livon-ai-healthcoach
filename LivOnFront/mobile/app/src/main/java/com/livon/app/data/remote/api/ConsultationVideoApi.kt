package com.livon.app.data.remote.api

import com.livon.app.core.network.RetrofitProvider
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Response DTO for video summary
data class VideoSummaryResponseDto(
    val consultationId: Long,
    val summary: String
)

interface ConsultationVideoRetrofitService {
    @Multipart
    @POST("consultations/video/{consultationId}/upload-and-summarize")
    suspend fun uploadAndSummarize(
        @Path("consultationId") consultationId: Long,
        @Part file: MultipartBody.Part,
        @Part("preQnA") preQnA: RequestBody? = null
    ): Unit

    @GET("gcp/video-summary/{consultationId}")
    suspend fun getSummary(
        @Path("consultationId") consultationId: Long
    ): ApiResponse<VideoSummaryResponseDto>
}

interface ConsultationVideoApi {
    suspend fun uploadAndSummarize(
        consultationId: Long,
        filePart: MultipartBody.Part,
        preQnA: RequestBody? = null
    )

    suspend fun getSummary(consultationId: Long): ApiResponse<VideoSummaryResponseDto>
}

class ConsultationVideoApiImpl(
    private val service: ConsultationVideoRetrofitService = RetrofitProvider.createService(ConsultationVideoRetrofitService::class.java)
) : ConsultationVideoApi {
    override suspend fun uploadAndSummarize(
        consultationId: Long,
        filePart: MultipartBody.Part,
        preQnA: RequestBody?
    ) {
        service.uploadAndSummarize(
            consultationId = consultationId,
            file = filePart,
            preQnA = preQnA
        )
    }

    override suspend fun getSummary(consultationId: Long): ApiResponse<VideoSummaryResponseDto> {
        return service.getSummary(consultationId)
    }
}


