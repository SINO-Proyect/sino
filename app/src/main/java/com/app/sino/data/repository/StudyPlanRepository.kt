package com.app.sino.data.repository

import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.RetrofitClient
import com.app.sino.data.remote.dto.*
import com.app.sino.data.util.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StudyPlanRepository(private val tokenManager: TokenManager? = null) {

    private val api = RetrofitClient.studyPlanApi

    suspend fun uploadPdfForExtraction(file: File): Resource<AiData> {
        return try {
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("pdf", file.name, requestFile)
            val format = "markdown".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadPdf(body, format)

            if (response.isSuccessful && response.body()?.ok == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "AI Extraction failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun saveStudyPlan(plan: StudyPlanDto, courses: List<CourseDto>): Resource<Boolean> {
        return try {
            val email = tokenManager?.getEmail() ?: return Resource.Error("User not logged in")
            
            // 1. Create Study Plan
            val planResponse = api.createStudyPlan(email, plan)
            if (!planResponse.isSuccessful || planResponse.body()?.success != true) {
                return Resource.Error("Failed to create Study Plan: ${planResponse.message()}")
            }

            val createdPlan = planResponse.body()!!.data!!
            val planId = createdPlan.idStudyPlan ?: return Resource.Error("Created plan has no ID")

            // 2. Assign ID to courses
            val coursesWithId = courses.map { it.copy(idStudyPlan = planId) }

            // 3. Save Courses
            val coursesResponse = api.createCourses(coursesWithId)
            if (coursesResponse.isSuccessful && coursesResponse.body()?.success == true) {
                Resource.Success(true)
            } else {
                Resource.Error("Plan created but courses failed: ${coursesResponse.message()}")
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error during save")
        }
    }

    suspend fun getStudyPlans(): Resource<List<StudyPlanDto>> {
        return try {
            val email = tokenManager?.getEmail() ?: return Resource.Error("User not logged in")
            val response = api.getStudyPlans(email)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching study plans")
        }
    }

    suspend fun getCourses(planId: Int): Resource<List<CourseDto>> {
        return try {
            val response = api.getCoursesByStudyPlan(planId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching courses")
        }
    }

    suspend fun getUniversities(): Resource<List<UniversityDto>> {
        return try {
           val response = api.getUniversities(size = 100)
           if (response.isSuccessful && response.body()?.success == true) {
               Resource.Success(response.body()!!.data?.content ?: emptyList())
           } else {
               Resource.Success(listOf(
                   UniversityDto(1, "Universidad Nacional (Mock)", "Costa Rica", true),
                   UniversityDto(2, "Universidad Privada (Mock)", "Costa Rica", true)
               ))
           }
        } catch (e: Exception) {
             Resource.Success(listOf(
                   UniversityDto(1, "Universidad Nacional (Offline)", "Costa Rica", true),
                   UniversityDto(2, "Universidad Privada (Offline)", "Costa Rica", true)
               ))
        }
    }
}