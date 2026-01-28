package com.app.sino.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.RetrofitClient
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.data.remote.dto.UserDto
import com.app.sino.data.repository.StudyPlanRepository
import com.app.sino.data.repository.UserRepository
import com.app.sino.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(RetrofitClient.userApi)
    private val studyRepository = StudyPlanRepository(TokenManager(application))
    private val tokenManager = TokenManager(application)

    private val _userState = MutableStateFlow<UserDto?>(null)
    val userState = _userState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _realProgress = MutableStateFlow(0f)
    val realProgress = _realProgress.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val email = tokenManager.getEmail()
        
        if (email != null) {
            viewModelScope.launch {
                _loading.value = true
                userRepository.syncUser(UserDto(email = email)).collect { result ->
                    if (result is Resource.Success<UserDto>) {
                        val user = result.data
                        _userState.value = user
                        
                        // Now that sync is done, get the confirmed userId
                        val userId = tokenManager.getUserId()
                        
                        if (userId != -1) {
                            val plansRes = studyRepository.getStudyPlans()
                            if (plansRes is Resource.Success) {
                                val activePlan = plansRes.data?.firstOrNull()
                                if (activePlan?.idStudyPlan != null) {
                                    val coursesRes = studyRepository.getCourses(activePlan.idStudyPlan)
                                    val studentCoursesRes = studyRepository.getStudentCourses(userId)
                                    
                                    if (coursesRes is Resource.Success && studentCoursesRes is Resource.Success) {
                                        val planCourses = coursesRes.data ?: emptyList()
                                        val studentCourses = studentCoursesRes.data ?: emptyList()
                                        
                                        val totalCredits = planCourses.sumOf { it.numCredits }
                                        val passedCredits = studentCourses.filter { it.idStatus == 4 }
                                            .sumOf { sc -> planCourses.find { it.idCourse == sc.idCourse }?.numCredits ?: 0 }
                                        
                                        if (totalCredits > 0) {
                                            _realProgress.value = passedCredits.toFloat() / totalCredits
                                        }
                                    }
                                }
                            }
                        }
                    }
                    _loading.value = false
                }
            }
        }
    }
}