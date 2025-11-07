package com.livon.app.feature.member.reservation.model

data class CoachUIModel(
    val id: String,
    val name: String,
    val job: String?,
    val intro: String,
    val avatarUrl: String?,
    val certificates: List<String> = emptyList(),
    val isCorporate: Boolean = false
)
