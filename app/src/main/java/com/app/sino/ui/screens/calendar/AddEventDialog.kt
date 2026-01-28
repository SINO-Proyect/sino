
package com.app.sino.ui.screens.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.sino.data.remote.dto.EventDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.app.sino.ui.screens.calendar.CalendarViewModel // Added import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    userId: Int,
    onDismiss: () -> Unit,
    onEventAdded: (EventDto, String) -> Unit,
    viewModel: CalendarViewModel
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var isTitleError by remember { mutableStateOf(false) } // State for title validation
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var typeEvent by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var isCourseCodeError by remember { mutableStateOf(false) } // State for course code validation

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage() // Clear the message after showing
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Nuevo Evento") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        isTitleError = false // Clear error when user types
                    },
                    label = { Text("Título") },
                    isError = isTitleError,
                    supportingText = { if (isTitleError) Text("El título no puede estar vacío") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Date Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val year = selectedDate.year
                            val month = selectedDate.monthValue - 1 // Month is 0-indexed
                            val day = selectedDate.dayOfMonth

                            DatePickerDialog(
                                context,
                                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                                    selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth)
                                }, year, month, day
                            ).show()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = selectedDate.format(dateFormatter),
                        onValueChange = { },
                        label = { Text("Fecha del evento") },
                        placeholder = { Text("Toca para seleccionar la fecha") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Time Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            val hour = selectedTime?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)
                            val minute = selectedTime?.minute ?: calendar.get(Calendar.MINUTE)

                            TimePickerDialog(
                                context,
                                { _, selectedHour: Int, selectedMinute: Int ->
                                    selectedTime = LocalTime.of(selectedHour, selectedMinute)
                                }, hour, minute, true
                            ).show()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = selectedTime?.format(timeFormatter) ?: "",
                        onValueChange = { },
                        label = { Text("Hora del evento (opcional)") },
                        placeholder = { Text("Toca para seleccionar la hora") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = "Seleccionar hora")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = typeEvent,
                    onValueChange = { typeEvent = it },
                    label = { Text("Tipo de Evento (examen, tarea, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = courseCode,
                    onValueChange = {
                        courseCode = it
                        isCourseCodeError = false // Clear error when user types
                    },
                    label = { Text("Código del Curso") },
                    isError = isCourseCodeError,
                    supportingText = { if (isCourseCodeError) Text("El código del curso no puede estar vacío") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isTitleError = title.isBlank()
                    isCourseCodeError = courseCode.isBlank()

                    if (!isTitleError && !isCourseCodeError) {
                        val event = EventDto(
                            idEvents = null, // Backend will generate this
                            idUser = userId,
                            idCourse = 0, // Placeholder, will be replaced by ViewModel after lookup
                            dscTitle = title,
                            dscDescription = description.ifBlank { null },
                            eventDate = selectedDate,
                            eventTime = selectedTime,
                            typeEvent = typeEvent,
                            status = "Pending" // Default status
                        )
                        onEventAdded(event, courseCode)
                        onDismiss()
                    }
                }
            ) {
                Text("Guardar Evento")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

