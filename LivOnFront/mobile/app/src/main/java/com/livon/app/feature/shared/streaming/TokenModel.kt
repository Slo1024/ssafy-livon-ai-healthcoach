package com.livon.app.feature.shared.streaming

import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(val consultationId: Long, val participantName: String)

// 서버 응답 구조: ApiResponse<Map<String, String>>
// {
//   "isSuccess": true,
//   "code": "string",
//   "message": "string",
//   "result": {
//     "token": "토큰값"
//   }
// }
@Serializable
data class TokenApiResponse(
    val isSuccess: Boolean,
    val code: String? = null,
    val message: String? = null,
    val result: Map<String, String>? = null
)