package com.app.sino.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.data.remote.dto.StudentCourseDto
import com.app.sino.data.remote.dto.StudyPlanDto
import com.app.sino.data.repository.StudyPlanRepository
import com.app.sino.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CourseWithStatus(
    val course: CourseDto,
    val studentCourse: StudentCourseDto?
)

sealed class CoursesUiState {
    object Loading : CoursesUiState()
    data class Success(val plans: List<StudyPlanDto>) : CoursesUiState()
    data class Error(val message: String) : CoursesUiState()
}

class CoursesViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository = StudyPlanRepository(tokenManager)

    private val _uiState = MutableStateFlow<CoursesUiState>(CoursesUiState.Loading)
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    private val _coursesWithStatus = MutableStateFlow<List<CourseWithStatus>>(emptyList())
    val coursesWithStatus: StateFlow<List<CourseWithStatus>> = _coursesWithStatus.asStateFlow()

    // Compatibility field
    private val _selectedPlanCourses = MutableStateFlow<List<CourseDto>>(emptyList())
    val selectedPlanCourses: StateFlow<List<CourseDto>> = _selectedPlanCourses.asStateFlow()

    private val _courseUpdateState = MutableStateFlow<Resource<Boolean>?>(null)
    val courseUpdateState: StateFlow<Resource<Boolean>?> = _courseUpdateState.asStateFlow()

    init {
        loadStudyPlans()
    }

    fun loadStudyPlans() {
        viewModelScope.launch {
            _uiState.value = CoursesUiState.Loading
            when (val result = repository.getStudyPlans()) {
                is Resource.Success -> {
                    _uiState.value = CoursesUiState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _uiState.value = CoursesUiState.Error(result.message ?: "Unknown Error")
                }
                else -> {}
            }
        }
    }

    fun loadCoursesForPlan(planId: Int) {
        viewModelScope.launch {
            var userId = tokenManager.getUserId()
            
            // If userId is missing, try to fetch user by email
            if (userId == -1) {
                val email = tokenManager.getEmail()
                if (email != null) {
                    val userResult = repository.getUserByEmail(email)
                    if (userResult is Resource.Success && userResult.data?.idUser != null) {
                        userId = userResult.data.idUser
                        tokenManager.saveUserId(userId)
                    }
                }
            }

            if (userId == -1) {
                _uiState.value = CoursesUiState.Error("User ID not found. Please log in again.")
                return@launch
            }

            val coursesResult = repository.getCourses(planId)
            val studentCoursesResult = repository.getStudentCourses(userId)

            if (coursesResult is Resource.Success && studentCoursesResult is Resource.Success) {
                val courses = coursesResult.data ?: emptyList()
                val studentCourses = studentCoursesResult.data ?: emptyList()

                val combined = courses.map { course ->
                    CourseWithStatus(
                        course = course,
                        studentCourse = studentCourses.find { it.idCourse == course.idCourse }
                    )
                }
                _coursesWithStatus.value = combined
                _selectedPlanCourses.value = courses
            } else {
                val errorMsg = when {
                    coursesResult is Resource.Error -> coursesResult.message
                    studentCoursesResult is Resource.Error -> studentCoursesResult.message
                    else -> "Failed to load courses"
                }
                _uiState.value = CoursesUiState.Error(errorMsg ?: "Unknown error")
            }
        }
    }

    fun updateCourseStatus(courseId: Int, newStatusId: Int) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            if (userId == -1) return@launch

            _courseUpdateState.value = Resource.Loading()
            
            val current = _coursesWithStatus.value.find { it.course.idCourse == courseId }?.studentCourse
            if (current == null) {
                _courseUpdateState.value = Resource.Error("Student course not found")
                return@launch
            }

            val updated = current.copy(idStatus = newStatusId)
            when (val result = repository.updateStudentCourse(updated)) {
                is Resource.Success -> {
                    // Trigger recalculation on backend if it was marked as passed
                    if (newStatusId == 4) {
                        repository.recalculateAllStatuses(userId)
                    }
                    // Reload everything to get fresh states
                    val activePlan = (uiState.value as? CoursesUiState.Success)?.plans?.firstOrNull()
                    activePlan?.idStudyPlan?.let { loadCoursesForPlan(it) }
                    _courseUpdateState.value = Resource.Success(true)
                }
                is Resource.Error -> {
                    _courseUpdateState.value = Resource.Error(result.message ?: "Error updating progress")
                }
                else -> {}
            }
        }
    }

    fun markCourseAsPassed(courseId: Int) {
        updateCourseStatus(courseId, 4)
    }

    fun updateCourse(course: CourseDto) {
        viewModelScope.launch {
            _courseUpdateState.value = Resource.Loading()
            when (val result = repository.updateCourse(course)) {
                is Resource.Success -> {
                    // Update local list
                    _coursesWithStatus.value = _coursesWithStatus.value.map { item ->
                        if (item.course.idCourse == course.idCourse) item.copy(course = result.data!!) else item
                    }
                    _selectedPlanCourses.value = _selectedPlanCourses.value.map { 
                        if (it.idCourse == course.idCourse) result.data!! else it 
                    }
                    _courseUpdateState.value = Resource.Success(true)
                }
                is Resource.Error -> {
                    _courseUpdateState.value = Resource.Error(result.message ?: "Error updating course")
                }
                else -> {}
            }
        }
    }

    fun resetUpdateState() {
        _courseUpdateState.value = null
    }

    fun getUserId(): Int {
        return tokenManager.getUserId()
    }
}