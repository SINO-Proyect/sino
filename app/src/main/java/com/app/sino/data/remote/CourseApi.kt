package com.app.sino.data.remote

import com.app.sino.data.remote.dto.ApiResponse
import com.app.sino.data.remote.dto.CourseDataDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CourseApi {

    @GET("courses/code/{code}")
    suspend fun getCourseByCode(@Path("code") code: String): ApiResponse<CourseDataDto>
}
