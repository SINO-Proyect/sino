
package com.app.sino.data.repository

import com.app.sino.data.remote.EventApi
import com.app.sino.data.remote.RetrofitClient
import com.app.sino.data.remote.dto.ApiResponse
import com.app.sino.data.remote.dto.EventDto

class EventRepository {
    private val eventApi: EventApi = RetrofitClient.eventApi

    suspend fun createEvent(event: EventDto): ApiResponse<EventDto> {
        return eventApi.createEvent(event)
    }

    suspend fun getEventsByUserId(userId: Int): ApiResponse<List<EventDto>> {
        return eventApi.getEventsByUserId(userId)
    }

    suspend fun updateEvent(id: Int, event: EventDto): ApiResponse<EventDto> {
        return eventApi.updateEvent(id, event)
    }

    suspend fun deleteEvent(id: Int): ApiResponse<String> {
        return eventApi.deleteEvent(id)
    }
}
