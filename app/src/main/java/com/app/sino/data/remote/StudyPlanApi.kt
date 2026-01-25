package com.app.sino.data.remote

import com.app.sino.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface StudyPlanApi {

    @Multipart
    @POST("ai/api/extract")
    suspend fun uploadPdf(
        @Part file: MultipartBody.Part,
        @Part("format") format: okhttp3.RequestBody
    ): Response<AiExtractionResponse>

    @POST("api/study-plans")
    suspend fun createStudyPlan(
        @Header("X-User-Email") userEmail: String,
        @Body plan: StudyPlanDto
    ): Response<ApiResponse<StudyPlanDto>>

    @POST("api/courses/batch")
    suspend fun createCourses(@Body courses: List<CourseDto>): Response<ApiResponse<List<CourseDto>>>
    
    @GET("api/study-plans/my")
    suspend fun getStudyPlans(
        @Header("X-User-Email") userEmail: String
    ): Response<ApiResponse<List<StudyPlanDto>>>

    @GET("api/courses/study-plan/{id}")
    suspend fun getCoursesByStudyPlan(@Path("id") id: Int): Response<ApiResponse<List<CourseDto>>>

    @GET("api/universities") 
    suspend fun getUniversities(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<ApiResponse<PageResponse<UniversityDto>>>

    @GET("api/universities")
    suspend fun searchUniversities(
        @Query("name") name: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<ApiResponse<PageResponse<UniversityDto>>>

    @POST("api/universities")
    suspend fun createUniversity(@Body university: UniversityDto): Response<ApiResponse<UniversityDto>>

    @PUT("api/courses/{id}")
    suspend fun updateCourse(@Path("id") id: Int, @Body course: CourseDto): Response<ApiResponse<CourseDto>>
}
