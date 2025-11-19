package com.livon.app.feature.shared.streaming.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 날짜/시간 포맷팅 유틸리티
 */
object DateFormatter {
    
    /**
     * 다양한 형식의 날짜 문자열을 한국 시간(KST) 기준 "오전/오후 HH:mm" 형식으로 변환
     * 지원 형식:
     * - "2025-11-19T14:31:10:010705817" (콜론 구분 나노초, 타임존 없음)
     * - "2025-11-04T12:34:13.421Z" (ISO 8601 표준)
     * - "2025-11-04T12:34:13Z"
     */
    fun formatToTime(sentAt: String): String {
        return try {
            val instant = parseToInstant(sentAt)
            
            // 한국 시간대(Asia/Seoul)로 변환
            val koreaZone = ZoneId.of("Asia/Seoul")
            val koreaTime = instant.atZone(koreaZone)
            
            // 시간 추출 (0-23)
            val hour = koreaTime.hour
            val minute = koreaTime.minute
            
            // 오전/오후 구분 및 12시간 형식으로 변환
            val period = if (hour < 12) "오전" else "오후"
            val displayHour = when {
                hour == 0 -> 12  // 자정
                hour > 12 -> hour - 12  // 오후 1시 이후
                else -> hour  // 오전 1시~11시
            }
            
            // "오전/오후 HH:mm" 형식으로 변환
            String.format("%s %02d:%02d", period, displayHour, minute)
        } catch (e: Exception) {
            // 파싱 실패 시 원본 반환 또는 기본값
            sentAt
        }
    }
    
    /**
     * 다양한 형식의 날짜 문자열을 Instant로 파싱
     */
    private fun parseToInstant(sentAt: String): Instant {
        return try {
            // 1. 표준 ISO 8601 형식 시도 (예: "2025-11-04T12:34:13.421Z")
            Instant.parse(sentAt)
        } catch (e: DateTimeParseException) {
            try {
                // 2. 콜론으로 구분된 나노초 형식 처리 (예: "2025-11-19T14:31:10:010705817")
                // 마지막 콜론을 점으로 변경 (시간 구분자는 유지)
                val lastColonIndex = sentAt.lastIndexOf(':')
                if (lastColonIndex > 0 && lastColonIndex < sentAt.length - 1) {
                    // 마지막 콜론을 점으로 변경
                    val normalized = sentAt.substring(0, lastColonIndex) + "." + sentAt.substring(lastColonIndex + 1)
                    val afterDatePart = if (normalized.length > 10) normalized.substring(10) else ""
                    val hasTimezone = normalized.endsWith("Z") || 
                                     afterDatePart.contains("+") || 
                                     afterDatePart.contains("-")
                    val withTimezone = if (!hasTimezone) {
                        normalized + "Z"
                    } else {
                        normalized
                    }
                    Instant.parse(withTimezone)
                } else {
                    throw e
                }
            } catch (e2: DateTimeParseException) {
                try {
                    // 3. 타임존이 없는 경우 UTC로 가정하고 파싱
                    // 나노초 부분 제거하고 초까지만 사용
                    // "2025-11-19T14:31:10:010705817" -> "2025-11-19T14:31:10"
                    val tIndex = sentAt.indexOf('T')
                    if (tIndex > 0) {
                        val timePart = sentAt.substring(tIndex + 1)
                        val secondsPart = timePart.split(':').take(3).joinToString(":")
                        val withoutNanos = sentAt.substring(0, tIndex + 1) + secondsPart + "Z"
                        Instant.parse(withoutNanos)
                    } else {
                        throw e2
                    }
                } catch (e3: DateTimeParseException) {
                    // 4. LocalDateTime으로 파싱 후 UTC로 가정
                    // 나노초 부분 제거
                    val tIndex = sentAt.indexOf('T')
                    if (tIndex > 0) {
                        val timePart = sentAt.substring(tIndex + 1)
                        val secondsPart = timePart.split(':').take(3).joinToString(":")
                        val cleanedDate = sentAt.substring(0, tIndex + 1) + secondsPart
                        val dateTime = LocalDateTime.parse(cleanedDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        dateTime.atZone(ZoneId.of("UTC")).toInstant()
                    } else {
                        throw e3
                    }
                }
            }
        }
    }
}


