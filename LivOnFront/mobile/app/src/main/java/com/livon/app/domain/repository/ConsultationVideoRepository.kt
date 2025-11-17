package com.livon.app.domain.repository

interface ConsultationVideoRepository {
    suspend fun getSummary(consultationId: Long): Result<String>
}

