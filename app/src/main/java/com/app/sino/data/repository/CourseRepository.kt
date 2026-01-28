package com.app.sino.data.repository

import com.app.sino.data.remote.CourseApi
import com.app.sino.data.remote.RetrofitClient
import com.app.sino.data.remote.dto.ApiResponse
import com.app.sino.data.remote.dto.CourseDataDto

class CourseRepository {
    private val courseApi: CourseApi = RetrofitClient.courseApi

    suspend fun getCourseByCode(courseCode: String): ApiResponse<CourseDataDto> {
        return courseApi.getCourseByCode(courseCode)
    }
}
