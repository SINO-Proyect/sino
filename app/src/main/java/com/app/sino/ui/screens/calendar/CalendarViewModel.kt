
package com.app.sino.ui.screens.calendar

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.sino.data.remote.dto.EventDto
import com.app.sino.data.repository.CourseRepository
import com.app.sino.data.repository.EventRepository
import com.app.sino.utils.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val eventRepository = EventRepository()
    private val courseRepository = CourseRepository()
    private val context = application.applicationContext

    private val _events = MutableStateFlow<List<EventDto>>(emptyList())
    val events: StateFlow<List<EventDto>> = _events

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun getEvents(userId: Int) {
        viewModelScope.launch {
            val response = eventRepository.getEventsByUserId(userId)
            if (response.success) {
                _events.value = response.data ?: emptyList()
            } else {
                _errorMessage.value = response.message ?: "Failed to fetch events."
            }
        }
    }

    fun createEvent(eventDto: EventDto, courseCode: String) {
        viewModelScope.launch {
            try {
                val courseResponse = courseRepository.getCourseByCode(courseCode)
                if (courseResponse.success && courseResponse.data != null) {
                    val actualEventDto = eventDto.copy(idCourse = courseResponse.data.idCourse)
                    val eventCreationResponse = eventRepository.createEvent(actualEventDto)
                    if (eventCreationResponse.success && eventCreationResponse.data != null) {
                        getEvents(actualEventDto.idUser)
                        NotificationScheduler.scheduleNotification(context, eventCreationResponse.data) // Schedule notification
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = eventCreationResponse.message ?: "Failed to create event."
                    }
                } else {
                    _errorMessage.value = courseResponse.message ?: "Course not found for code: $courseCode"
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }

    fun updateEvent(eventDto: EventDto, courseCode: String) {
        viewModelScope.launch {
            try {
                if (eventDto.idEvents == null) {
                    _errorMessage.value = "Event ID is required to update an event."
                    return@launch
                }
                val courseResponse = courseRepository.getCourseByCode(courseCode)
                if (courseResponse.success && courseResponse.data != null) {
                    val actualEventDto = eventDto.copy(idCourse = courseResponse.data.idCourse)
                    // Cancel old notification before updating
                    NotificationScheduler.cancelNotification(context, actualEventDto.idEvents!!)
                    val eventUpdateResponse = eventRepository.updateEvent(actualEventDto.idEvents!!, actualEventDto)
                    if (eventUpdateResponse.success && eventUpdateResponse.data != null) {
                        getEvents(actualEventDto.idUser)
                        NotificationScheduler.scheduleNotification(context, eventUpdateResponse.data) // Schedule new notification
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = eventUpdateResponse.message ?: "Failed to update event."
                    }
                } else {
                    _errorMessage.value = courseResponse.message ?: "Course not found for code: $courseCode"
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }

    fun deleteEvent(eventId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                // Cancel notification before deleting
                NotificationScheduler.cancelNotification(context, eventId)
                val eventDeleteResponse = eventRepository.deleteEvent(eventId)
                if (eventDeleteResponse.success) {
                    getEvents(userId)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = eventDeleteResponse.message ?: "Failed to delete event."
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
