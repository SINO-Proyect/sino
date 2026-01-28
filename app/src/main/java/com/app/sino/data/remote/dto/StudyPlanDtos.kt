package com.app.sino.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StudyPlanDto(
    val idStudyPlan: Int? = null,
    val idUniversity: Int,
    val dscName: String,
    val dscCareer: String,
    val typePeriod: String, // SEMESTER, TRIMESTER, ANNUAL
    val yearLevel: String,
    val status: Boolean = true
)

data class CourseDto(
    val idCourse: Int? = null,
    val idStudyPlan: Int? = null,
    val dscCode: String,
    val dscName: String,
    val dscLevel: String,
    val dscPeriod: String,
    val typeCourse: String, // Diplomado, Bachillerato, Licenciatura, etc.
    val numCredits: Int,
    val description: String? = null,
    val prerequisites: List<String>? = null,
    val corequisites: List<String>? = null
)

data class AiExtractionResponse(
    val ok: Boolean,
    val data: AiData?
)

data class AiData(
    @SerializedName("study_plan") val studyPlan: StudyPlanDto,
    @SerializedName("courses") val courses: List<CourseDto>
)

data class UniversityDto(
    val idUniversity: Int,
    val dscName: String,
    val dscCountry: String? = null,
    val status: Boolean = true
)

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String
)
