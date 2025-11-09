package com.livon.app.data.local

import com.livon.app.feature.member.reservation.ui.ReservationUi
import java.time.LocalDate
import java.util.UUID

/**
 * Debug/dev-only in-memory reservation store.
 * Additions here are used to reflect newly created reservations in the UI while developing.
 * This is intentionally simple and not persisted. Marked as dev-only so it can be removed later.
 */
object LocalReservationStore {
    private val items: MutableList<ReservationUi> = mutableListOf()

    fun addReservation(coachName: String, date: LocalDate, timeText: String? = null, className: String = "개인 상담", classIntro: String = "") {
        val id = UUID.randomUUID().toString()
        val reservation = ReservationUi(
            id = id,
            date = date,
            className = className,
            coachName = coachName,
            coachRole = "",
            coachIntro = classIntro,
            timeText = timeText ?: "미정",
            classIntro = classIntro
        )
        items.add(0, reservation) // newest first
    }

    fun getAll(): List<ReservationUi> = items.toList()

    fun clear() = items.clear()
}

