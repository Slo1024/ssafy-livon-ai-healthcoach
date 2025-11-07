package com.livon.app.feature.shared.streaming.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 날짜/시간 포맷팅 유틸리티
 */
object DateFormatter {
    
    /**
     * ISO 8601 형식의 날짜 문자열을 "HH:mm" 형식으로 변환
     * 예: "2025-11-04T12:34:13.421Z" -> "12:34"
     */
    fun formatToTime(sentAt: String): String {
        return try {
            // ISO 8601 형식 파싱 (Z 또는 타임존 제거)
            val cleanedDate = sentAt.replace("Z", "").substringBefore(".")
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val dateTime = LocalDateTime.parse(cleanedDate, formatter)
            
            // "HH:mm" 형식으로 변환
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: DateTimeParseException) {
            // 파싱 실패 시 원본 반환 또는 기본값
            sentAt
        }
    }
}


