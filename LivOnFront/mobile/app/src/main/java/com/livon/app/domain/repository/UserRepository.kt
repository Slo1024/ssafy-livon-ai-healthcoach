package com.livon.app.domain.repository

import android.util.Log
import com.livon.app.data.remote.api.HealthSurveyResponse
import com.livon.app.data.remote.api.MyInfoResponse
import com.livon.app.data.remote.api.UserApiService
import com.livon.app.feature.member.my.MyInfoUiState
import com.livon.app.data.remote.api.ProfileImageRequest
import com.livon.app.data.remote.api.HealthSurveyRequest
import androidx.core.net.toUri

class UserRepository(private val api: UserApiService) {
    // (RetrofitProvider.moshi is available if needed elsewhere)

    suspend fun getMyInfo(): Result<MyInfoUiState> {
        return try {
            val res = api.getMyInfo()
            Log.d("UserRepository", "getMyInfo: isSuccess=${'$'}{res.isSuccess}, msg=${'$'}{res.message}")
            if (res.isSuccess && res.result != null) {
                val r: MyInfoResponse = res.result

                // Debug: log raw healthSurvey payload and its runtime type to aid diagnosing parsing issues
                try {
                    val hsClass = r.healthSurvey?.let { it::class.qualifiedName } ?: "null"
                    Log.d("UserRepository", "raw healthSurvey class=$hsClass raw=${'$'}{r.healthSurvey}")
                } catch (t: Throwable) {
                    Log.d("UserRepository", "failed to log raw healthSurvey: ${'$'}{t.message}")
                }

                // parse healthSurvey which can be: null, empty list, object (Map), JSON string, or list with single object
                var health: HealthSurveyResponse? = null
                try {
                    val rawMap = r.healthSurvey
                    if (rawMap != null) {
                        health = parseHealthFromAny(rawMap)
                    }
                } catch (t: Throwable) {
                    Log.d("UserRepository", "health parse error: ${'$'}{t.message}")
                    Log.d("UserRepository", Log.getStackTraceString(t))
                }

                // Debug: log parsed health values to help trace why height/weight may not be appearing in UI
                Log.d("UserRepository", "parsed health -> height=${'$'}{health?.height}, weight=${'$'}{health?.weight}")

                // Format height/weight to remove trailing .0 (e.g., 170.0 -> "170") so UI displays cleaner numbers and matches existing expectations.
                fun formatDoubleToIntString(d: Double?): String? {
                    if (d == null) return null
                    return if (d % 1.0 == 0.0) d.toInt().toString() else d.toString()
                }

                val state = MyInfoUiState(
                    nickname = r.nickname,
                    gender = r.gender,
                    birthday = r.birthdate,
                    profileImageUri = r.profileImage?.toUri(),
                    heightCm = formatDoubleToIntString(health?.height),
                    weightKg = formatDoubleToIntString(health?.weight),
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
            Log.d("UserRepository", "exception: ${'$'}{t.message}")
            Result.failure(t)
        }
    }

    suspend fun updateProfileImage(profileImageUrl: String): Result<Boolean> {
        return try {
            val req = ProfileImageRequest(profileImageUrl)
            val res = api.updateProfileImage(req)
            Log.d("UserRepository", "updateProfileImage: isSuccess=${'$'}{res.isSuccess}, msg=${'$'}{res.message}")
            if (res.isSuccess) Result.success(true) else Result.failure(Exception(res.message ?: "update failed"))
        } catch (t: Throwable) {
            Log.d("UserRepository", "updateProfileImage exception: ${'$'}{t.message}")
            Result.failure(t)
        }
    }

    // New: post health survey to backend
    suspend fun postHealthSurvey(req: HealthSurveyRequest): Result<Boolean> {
        return try {
            val res = api.postHealthSurvey(req)
            Log.d("UserRepository", "postHealthSurvey: isSuccess=${'$'}{res.isSuccess}, msg=${'$'}{res.message}")
            if (res.isSuccess) Result.success(true) else Result.failure(Exception(res.message ?: "postHealthSurvey failed"))
        } catch (t: Throwable) {
            Log.d("UserRepository", "postHealthSurvey exception: ${'$'}{t.message}")
            Result.failure(t)
        }
    }

    // --- helper parsing functions ---
    // parse arbitrary Any (Map/List/String) into HealthSurveyResponse by flattening nested structures
    private fun parseHealthFromAny(raw: Any): HealthSurveyResponse {
        return when (raw) {
            is Map<*, *> -> parseHealthFromMap(raw)
            is String -> {
                // attempt to parse JSON string into a Map using simple heuristics: try to detect numeric pairs like "height":170
                // fallback: attempt to extract numbers from the string
                val numbers = raw.filter { it.isDigit() || it == '.' || it == ',' || it.isWhitespace() }
                val maybeList = numbers.split(Regex("[\\s,]+")).mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
                val h = maybeList.getOrNull(0)?.replace(",", ".")?.toDoubleOrNull()
                val w = maybeList.getOrNull(1)?.replace(",", ".")?.toDoubleOrNull()
                HealthSurveyResponse(
                    weight = w,
                    height = h,
                    steps = null,
                    sleepTime = null,
                    disease = null,
                    sleepQuality = null,
                    medicationsInfo = null,
                    painArea = null,
                    stressLevel = null,
                    smokingStatus = null,
                    avgSleepHours = null,
                    activityLevel = null,
                    caffeineIntakeLevel = null
                )
            }
            is List<*> -> {
                // if list has a single map, parse that
                val firstMap = raw.firstOrNull { it is Map<*, *> } as? Map<*, *>
                if (firstMap != null) parseHealthFromMap(firstMap) else HealthSurveyResponse(null, null, null, null, null, null, null, null, null, null, null, null, null)
            }
            else -> HealthSurveyResponse(null, null, null, null, null, null, null, null, null, null, null, null, null)
        }
    }

    private fun parseHealthFromMap(mapRaw: Map<*, *>): HealthSurveyResponse {
        // Flatten nested maps/lists into a list of key->value candidates
        val flattened = mutableMapOf<String, Any?>()

        fun normalizeKey(k: String): String = k.replace(Regex("[^A-Za-z0-9]"), "").lowercase()

        fun visit(prefix: String?, v: Any?) {
            when (v) {
                null -> return
                is Map<*, *> -> {
                    v.forEach { (k, vv) ->
                        val key = (k?.toString() ?: "").let { if (prefix.isNullOrBlank()) it else "${prefix}_${it}" }
                        visit(key, vv)
                    }
                }
                is List<*> -> {
                    v.forEachIndexed { idx, item ->
                        val key = if (prefix.isNullOrBlank()) "item$idx" else "${prefix}_item$idx"
                        visit(key, item)
                    }
                }
                else -> {
                    val key = normalizeKey(prefix ?: "")
                    if (key.isNotBlank()) flattened[key] = v
                }
            }
        }

        // seed
        mapRaw.forEach { (k, v) -> visit(k?.toString(), v) }

        // For debugging, log flattened keys if none of height/weight found
        // Helper to find numeric by scanning keys and values
        fun findNumeric(vararg candidates: String): Double? {
            val candNorm = candidates.map { it.replace(Regex("[^A-Za-z0-9]"), "").lowercase() }
            // 1) direct key match
            for ((key, value) in flattened) {
                if (candNorm.contains(key)) {
                    when (value) {
                        is Number -> return value.toDouble()
                        is String -> {
                            val cleaned = value.trim()
                            cleaned.toDoubleOrNull()?.let { return it }
                            val digits = cleaned.filter { it.isDigit() }
                            if (digits.isNotEmpty()) return digits.toDoubleOrNull()
                        }
                    }
                }
            }

            // 2) loose search: keys containing candidate as substring
            for ((key, value) in flattened) {
                for (cand in candNorm) {
                    if (key.contains(cand)) {
                        when (value) {
                            is Number -> return value.toDouble()
                            is String -> {
                                val cleaned = value.trim()
                                cleaned.toDoubleOrNull()?.let { return it }
                                val digits = cleaned.filter { it.isDigit() }
                                if (digits.isNotEmpty()) return digits.toDoubleOrNull()
                            }
                        }
                    }
                }
            }

            // 3) try values themselves if they look like numbers
            for ((_, value) in flattened) {
                when (value) {
                    is Number -> return value.toDouble()
                    is String -> {
                        val cleaned = value.trim()
                        cleaned.toDoubleOrNull()?.let { return it }
                    }
                }
            }

            return null
        }

        val h = findNumeric("height", "heightcm", "height_cm", "ht")
        val w = findNumeric("weight", "weightkg", "weight_kg", "wt")

        return HealthSurveyResponse(
            weight = w,
            height = h,
            steps = (flattened["steps"] as? Number)?.toInt(),
            sleepTime = (flattened["sleeptime"] as? Number)?.toDouble(),
            disease = flattened["disease"] as? String,
            sleepQuality = flattened["sleepquality"] as? String,
            medicationsInfo = flattened["medicationsinfo"] as? String,
            painArea = flattened["painarea"] as? String,
            stressLevel = flattened["stresslevel"] as? String,
            smokingStatus = flattened["smokingstatus"] as? String,
            avgSleepHours = (flattened["avgsleephours"] as? Number)?.toDouble(),
            activityLevel = flattened["activitylevel"] as? String,
            caffeineIntakeLevel = flattened["caffeineintakelevel"] as? String
        )
    }
}
