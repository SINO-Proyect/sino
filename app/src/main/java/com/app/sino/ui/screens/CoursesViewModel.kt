package com.app.sino.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.data.remote.dto.StudyPlanDto
import com.app.sino.data.repository.StudyPlanRepository
import com.app.sino.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
            when (val result = repository.getCourses(planId)) {
                is Resource.Success -> {
                    _selectedPlanCourses.value = result.data ?: emptyList()
                }
                else -> {}
            }
        }
    }

    fun updateCourse(course: CourseDto) {
        viewModelScope.launch {
            _courseUpdateState.value = Resource.Loading()
            when (val result = repository.updateCourse(course)) {
                is Resource.Success -> {
                    // Update local list
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
}