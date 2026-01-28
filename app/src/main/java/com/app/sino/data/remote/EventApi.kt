
package com.app.sino.data.remote

import com.app.sino.data.remote.dto.EventDto
import com.app.sino.data.remote.dto.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT // Added import
import retrofit2.http.DELETE // Added import

interface EventApi {

    @POST("events")
    suspend fun createEvent(@Body event: EventDto): ApiResponse<EventDto>

    @GET("events/user/{userId}")
    suspend fun getEventsByUserId(@Path("userId") userId: Int): ApiResponse<List<EventDto>>

    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") id: Int, @Body event: EventDto): ApiResponse<EventDto>

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): ApiResponse<String>
}
