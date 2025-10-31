package com.livon.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Upcoming(
    val id: String,
    val title: String,
    val date: LocalDate,
    val time: LocalTime
)
