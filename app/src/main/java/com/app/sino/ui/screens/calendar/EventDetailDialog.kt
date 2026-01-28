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
fun EventDetailDialog(
    event: EventDto,
    userId: Int,
    onDismiss: () -> Unit,
    onUpdateEvent: (EventDto, String) -> Unit,
    onDeleteEvent: (Int) -> Unit,
    viewModel: CalendarViewModel
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(event.dscTitle) }
    var isTitleError by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(event.dscDescription ?: "") }
    var selectedDate by remember { mutableStateOf(event.eventDate) }
    var selectedTime by remember { mutableStateOf(event.eventTime) }
    var typeEvent by remember { mutableStateOf(event.typeEvent) }
    var courseCode by remember { mutableStateOf("") } // Course code is not directly in EventDto
    var isCourseCodeError by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle del Evento") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        isTitleError = false
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val year = selectedDate.year
                            val month = selectedDate.monthValue - 1
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
                        isCourseCodeError = false
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
                        val updatedEvent = event.copy(
                            dscTitle = title,
                            dscDescription = description.ifBlank { null },
                            eventDate = selectedDate,
                            eventTime = selectedTime,
                            typeEvent = typeEvent,
                            status = "Pending" // Retain default status or allow editing
                        )
                        onUpdateEvent(updatedEvent, courseCode)
                        onDismiss()
                    }
                }
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        event.idEvents?.let { onDeleteEvent(it) }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar Evento")
                }
                Button(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}