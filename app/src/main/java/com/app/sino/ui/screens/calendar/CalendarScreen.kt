
package com.app.sino.ui.screens.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.ui.components.BottomNavigationBar
import com.app.sino.data.remote.dto.EventDto
import com.app.sino.ui.screens.calendar.CalendarViewModel
import io.github.boguszpawlowski.composecalendar.StaticCalendar
import io.github.boguszpawlowski.composecalendar.day.Day
import io.github.boguszpawlowski.composecalendar.rememberCalendarState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import android.app.Application // Added import
import androidx.compose.ui.platform.LocalContext // Added import
import androidx.lifecycle.ViewModelProvider // Added import

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: CalendarViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CalendarViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    })

    val events by viewModel.events.collectAsState()
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showEventDetailDialog by remember { mutableStateOf<Boolean>(false) }
    var selectedEventForDetail by remember { mutableStateOf<EventDto?>(null) }
    val calendarState = rememberCalendarState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

// Filter events for the selected date
    val filteredEvents = events.filter { it.eventDate == selectedDate }

    val currentUserId = 1
    viewModel.getEvents(currentUserId)

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            StaticCalendar(
                calendarState = calendarState,
                modifier = Modifier.padding(8.dp),
                dayContent = { day: Day ->
                    val hasEvent = events.any { it.eventDate == day.date }
                    val isSelected = selectedDate == day.date  // Comparar con tu estado local

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(4.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable {
                                // Actualizar tu estado local
                                selectedDate = day.date
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (hasEvent) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .height(6.dp)
                                    .fillMaxWidth(0.5f)
                            )
                        }
                    }
                }
            )

            Button(
                onClick = { showAddEventDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Agregar Evento")
            }

            // Display events for the selected date
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Eventos para el ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (filteredEvents.isEmpty()) {
                    Text("No hay eventos para esta fecha.")
                } else {
                    LazyColumn {
                        items(filteredEvents) { event ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedEventForDetail = event
                                        showEventDetailDialog = true
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = event.dscTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    event.dscDescription?.let {
                                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    event.eventTime?.let {
                                        Text(text = "Hora: ${it.format(DateTimeFormatter.ofPattern("HH:mm"))}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(text = "Tipo: ${event.typeEvent}", style = MaterialTheme.typography.bodySmall)
                                    event.status?.let {
                                        Text(text = "Estado: $it", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddEventDialog) {
            AddEventDialog(
                userId = currentUserId,
                onDismiss = { showAddEventDialog = false },
                onEventAdded = { eventDto, courseCode ->
                    viewModel.createEvent(eventDto, courseCode)
                },
                viewModel = viewModel // Pass the ViewModel explicitly
            )
        }

        if (showEventDetailDialog && selectedEventForDetail != null) {
            EventDetailDialog(
                event = selectedEventForDetail!!,
                userId = currentUserId,
                onDismiss = { showEventDetailDialog = false },
                onUpdateEvent = { updatedEventDto, courseCode ->
                    viewModel.updateEvent(updatedEventDto, courseCode)
                    showEventDetailDialog = false
                },
                onDeleteEvent = { eventId ->
                    viewModel.deleteEvent(eventId, currentUserId)
                    showEventDetailDialog = false
                },
                viewModel = viewModel // Pass the ViewModel explicitly
            )
        }
    }
}
