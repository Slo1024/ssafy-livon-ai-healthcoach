package com.livon.app.feature.shared.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

object SignupState {
    var role: String by mutableStateOf("member")
    var nickname: String by mutableStateOf("")
    var passwordSet: Boolean by mutableStateOf(false)
    var heightCm: String? by mutableStateOf(null)
    var weightKg: String? by mutableStateOf(null)
    var birthday: LocalDate? by mutableStateOf(null)

    // health survey answers
    var condition: String? by mutableStateOf(null)
    var sleepQuality: String? by mutableStateOf(null)
    var medication: String? by mutableStateOf(null)
    var painArea: String? by mutableStateOf(null)
    var stress: String? by mutableStateOf(null)

    // lifestyle
    var smoking: String? by mutableStateOf(null)
    var alcohol: String? by mutableStateOf(null)
    var sleepHours: String? by mutableStateOf(null)
    var activityLevel: String? by mutableStateOf(null)
    var caffeine: String? by mutableStateOf(null)

    fun clear() {
        role = "member"
        nickname = ""
        passwordSet = false
        heightCm = null
        weightKg = null
        birthday = null
        condition = null
        sleepQuality = null
        medication = null
        painArea = null
        stress = null
        smoking = null
        alcohol = null
        sleepHours = null
        activityLevel = null
        caffeine = null
    }
}
