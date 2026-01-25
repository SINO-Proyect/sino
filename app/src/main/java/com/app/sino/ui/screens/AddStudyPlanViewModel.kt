package com.app.sino.ui.screens

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.data.remote.dto.StudyPlanDto
import com.app.sino.data.remote.dto.UniversityDto
import com.app.sino.data.repository.StudyPlanRepository
import com.app.sino.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class LocalCycle(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val courses: List<LocalCourse> = emptyList()
)

data class LocalCourse(
    val tempId: String = UUID.randomUUID().toString(),
    val name: String,
    val code: String,
    val credits: Int,
    val isObligatory: Boolean = true,
    val prerequisitesIds: List<String> = emptyList(),
    val corequisitesIds: List<String> = emptyList(),
    val description: String = ""
)

class AddStudyPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val repository = StudyPlanRepository(tokenManager)

    private val _uiState = MutableStateFlow<AddPlanUiState>(AddPlanUiState.Idle)
    val uiState: StateFlow<AddPlanUiState> = _uiState.asStateFlow()
    
    private val _universities = MutableStateFlow<List<UniversityDto>>(emptyList())
    val universities: StateFlow<List<UniversityDto>> = _universities.asStateFlow()

    var selectedUniversityId = MutableStateFlow<Int?>(null)
    var planName = MutableStateFlow("")
    var careerName = MutableStateFlow("")
    var yearLevel = MutableStateFlow("")
    var periodType = MutableStateFlow("Semestre")

    private val _cycles = MutableStateFlow<List<LocalCycle>>(emptyList())
    val cycles: StateFlow<List<LocalCycle>> = _cycles.asStateFlow()

    init {
        loadUniversities()
        addCycle("I", "1")
    }

    private fun loadUniversities() {
        viewModelScope.launch {
            when(val result = repository.getUniversities()) {
                is Resource.Success -> {
                    val unis = result.data ?: emptyList()
                    _universities.value = unis
                }
                else -> {}
            }
        }
    }

    fun searchUniversities(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadUniversities()
                return@launch
            }
            when(val result = repository.searchUniversities(query)) {
                is Resource.Success -> {
                    _universities.value = result.data ?: emptyList()
                }
                else -> {}
            }
        }
    }

    fun createAndSelectUniversity(name: String, country: String = "Costa Rica") {
        viewModelScope.launch {
            _uiState.value = AddPlanUiState.Loading("Creando Universidad...")
            when(val result = repository.createUniversity(name, country)) {
                is Resource.Success -> {
                    val newUni = result.data!!
                    _universities.update { current ->
                        current + newUni
                    }
                    selectedUniversityId.value = newUni.idUniversity
                    _uiState.value = AddPlanUiState.Success("Universidad creada")
                }
                is Resource.Error -> {
                    _uiState.value = AddPlanUiState.Error(result.message ?: "Error al crear universidad")
                }
                else -> {}
            }
        }
    }

    fun addCycle(degree: String, number: String) {
        val cycleName = "$degree $number ${periodType.value}"
        _cycles.update { it + LocalCycle(name = cycleName) }
    }

    fun removeCycle(cycleId: String) {
        _cycles.update { it.filter { cycle -> cycle.id != cycleId } }
    }

    fun renameCycle(cycleId: String, newName: String) {
        _cycles.update { list ->
            list.map { if (it.id == cycleId) it.copy(name = newName) else it }
        }
    }

    fun updatePeriodType(newType: String) {
        periodType.value = newType
        _cycles.update { list ->
            list.mapIndexed { index, cycle ->
                if (cycle.name.contains("Semestre") || cycle.name.contains("Cuatrimestre") || cycle.name.contains("Trimestre") || cycle.name.contains("Ciclo")) {
                    cycle.copy(name = "${index + 1} $newType")
                } else {
                    cycle
                }
            }
        }
    }

    fun addCourseToCycle(cycleId: String, course: LocalCourse) {
        _cycles.update { list ->
            list.map { cycle ->
                if (cycle.id == cycleId) {
                    cycle.copy(courses = cycle.courses + course)
                } else {
                    cycle
                }
            }
        }
    }

    fun updateCourse(cycleId: String, updatedCourse: LocalCourse) {
         _cycles.update { list ->
            list.map { cycle ->
                if (cycle.id == cycleId) {
                    cycle.copy(courses = cycle.courses.map { if (it.tempId == updatedCourse.tempId) updatedCourse else it })
                } else {
                    cycle
                }
            }
        }
    }

    fun removeCourse(cycleId: String, courseId: String) {
        _cycles.update { list ->
            list.map { cycle ->
                if (cycle.id == cycleId) {
                    cycle.copy(courses = cycle.courses.filter { it.tempId != courseId })
                } else {
                    cycle
                }
            }
        }
        cleanPrerequisites(courseId)
    }

    private fun cleanPrerequisites(removedCourseId: String) {
        _cycles.update { list ->
            list.map { cycle ->
                cycle.copy(courses = cycle.courses.map { course ->
                    course.copy(
                        prerequisitesIds = course.prerequisitesIds.filter { it != removedCourseId },
                        corequisitesIds = course.corequisitesIds.filter { it != removedCourseId }
                    )
                })
            }
        }
    }

    fun getAvailablePrerequisites(currentCycleId: String): List<LocalCourse> {
        val allCycles = _cycles.value
        val currentIndex = allCycles.indexOfFirst { it.id == currentCycleId }
        if (currentIndex <= 0) return emptyList()
        return allCycles.subList(0, currentIndex).flatMap { it.courses }
    }

    fun onPdfSelected(uri: Uri, context: Context) {

    }

    fun validateGeneralInfo(): Boolean {
        if (selectedUniversityId.value == null) {
            _uiState.value = AddPlanUiState.Error("Debes seleccionar una universidad")
            return false
        }
        if (planName.value.isBlank()) {
            _uiState.value = AddPlanUiState.Error("El nombre del plan es requerido")
            return false
        }
        if (planName.value.length > 200) {
            _uiState.value = AddPlanUiState.Error("El nombre del plan es muy largo (máx 200)")
            return false
        }
        if (careerName.value.isBlank()) {
            _uiState.value = AddPlanUiState.Error("El nombre de la carrera es requerido")
            return false
        }
        if (careerName.value.length > 200) {
            _uiState.value = AddPlanUiState.Error("El nombre de la carrera es muy largo (máx 200)")
            return false
        }
        if (yearLevel.value.isBlank()) {
            _uiState.value = AddPlanUiState.Error("El año o nivel es requerido")
            return false
        }
        if (yearLevel.value.length > 20) {
            _uiState.value = AddPlanUiState.Error("El año es muy largo")
            return false
        }
        if (yearLevel.value.toIntOrNull() == null) {
             _uiState.value = AddPlanUiState.Error("El año debe ser un número válido")
            return false
        }
        return true
    }

    fun savePlan() {
        if (!validateGeneralInfo()) return

        val uniId = selectedUniversityId.value!! // Checked in validateGeneralInfo
        
        if (_cycles.value.isEmpty() || _cycles.value.all { it.courses.isEmpty() }) {
             _uiState.value = AddPlanUiState.Error("El plan debe tener al menos un curso")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddPlanUiState.Loading("Guardando Plan de Estudios...")
            
            val planDto = StudyPlanDto(
                idUniversity = uniId,
                dscName = planName.value,
                dscCareer = careerName.value,
                typePeriod = periodType.value,
                yearLevel = yearLevel.value,
                status = true
            )

            val idToCodeMap = _cycles.value.flatMap { it.courses }.associate { it.tempId to it.code }

            val allCoursesDto = _cycles.value.flatMap { cycle ->
                cycle.courses.map { local ->
                    CourseDto(
                        dscCode = local.code,
                        dscName = local.name,
                        dscLevel = cycle.name,
                        dscPeriod = cycle.name,
                        typeCourse = if (local.isObligatory) "OBLIGATORY" else "ELECTIVE",
                        numCredits = local.credits,
                        requirement = local.prerequisitesIds.isNotEmpty(),
                        description = local.description,
                        prerequisites = local.prerequisitesIds.mapNotNull { idToCodeMap[it] }
                    )
                }
            }

            when (val result = repository.saveStudyPlan(planDto, allCoursesDto)) {
                is Resource.Success -> {
                    _uiState.value = AddPlanUiState.Saved
                }
                is Resource.Error -> {
                    _uiState.value = AddPlanUiState.Error(result.message ?: "Error al guardar")
                }
                else -> {}
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AddPlanUiState.Idle
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val myFile = File(context.cacheDir, getFileName(uri, contentResolver))
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(myFile)
        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
        return myFile
    }

    private fun getFileName(uri: Uri, contentResolver: android.content.ContentResolver): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use { if (it != null && it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(index >= 0) result = it.getString(index)
            }}
        }
        return result ?: "temp.pdf"
    }
}

sealed class AddPlanUiState {
    object Idle : AddPlanUiState()
    data class Loading(val message: String) : AddPlanUiState()
    data class Success(val message: String) : AddPlanUiState()
    data class Error(val message: String) : AddPlanUiState()
    object Saved : AddPlanUiState()
}