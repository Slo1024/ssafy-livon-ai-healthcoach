package com.livon.app.domain.repository

import com.livon.app.domain.model.Upcoming

interface HomeRepository {
    suspend fun fetchUpcoming(limit: Int = 5): List<Upcoming>
}
