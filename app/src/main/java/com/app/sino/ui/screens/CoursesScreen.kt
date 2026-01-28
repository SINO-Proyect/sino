package com.app.sino.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.data.remote.dto.StudyPlanDto
import com.app.sino.data.util.Resource
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.theme.SinoPrimary
import com.app.sino.ui.util.romanToDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    onAddPlanClick: () -> Unit,
    viewModel: CoursesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.courseUpdateState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadStudyPlans()
    }

    LaunchedEffect(updateState) {
        when(updateState) {
            is Resource.Success -> {
                Toast.makeText(context, "Curso actualizado exitosamente", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
            }
            is Resource.Error -> {
                Toast.makeText(context, updateState?.message ?: "Error", Toast.LENGTH_LONG).show()
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is CoursesUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = SinoPrimary
                )
            }
            is CoursesUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    SinoButton(text = "Reintentar", onClick = { viewModel.loadStudyPlans() })
                }
            }
            is CoursesUiState.Success -> {
                if (state.plans.isEmpty()) {
                    EmptyPlanState(onAddPlanClick)
                } else {
                    val activePlan = state.plans.first()
                    ActivePlanView(activePlan, viewModel)
                }
            }
        }
    }
}

@Composable
fun EmptyPlanState(onAddPlanClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(SinoPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.book_open_duotone),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = SinoPrimary
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Sin plan de estudio",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Parece que aún no has configurado tu camino académico. Crea un plan para empezar a seguir tu progreso.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        SinoButton(
            text = "Configurar Plan de Estudio",
            onClick = onAddPlanClick
        )
    }
}

@Composable
fun ActivePlanView(plan: StudyPlanDto, viewModel: CoursesViewModel) {
    val coursesWithStatus by viewModel.coursesWithStatus.collectAsState()
    val context = LocalContext.current
    var courseToEdit by remember { mutableStateOf<CourseDto?>(null) }
    
    val currentUserId = viewModel.getUserId()
    val canEdit = plan.idCreator == currentUserId || plan.idCreator == null

    LaunchedEffect(plan.idStudyPlan) {
        if (coursesWithStatus.isEmpty() && plan.idStudyPlan != null) {
            viewModel.loadCoursesForPlan(plan.idStudyPlan)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PlanHeaderCard(
                plan = plan, 
                coursesWithStatus = coursesWithStatus,
                canEdit = canEdit,
                onEdit = { Toast.makeText(context, "Edición de plan próximamente", Toast.LENGTH_SHORT).show() }
            )
        }

        if (coursesWithStatus.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SinoPrimary)
                }
            }
        } else {
            val grouped = coursesWithStatus.groupBy { "${it.course.dscLevel}|${it.course.dscPeriod}" }
            val sortedKeys = grouped.keys.sortedWith(compareBy({ it.split("|")[0].romanToDecimal() }, { it.split("|")[1].filter { c -> c.isDigit() }.toIntOrNull() ?: 999 }))

            sortedKeys.forEach { key ->
                val items = grouped[key] ?: emptyList()
                val parts = key.split("|")
                val level = parts[0]
                val period = parts[1]
                
                item {
                    Text(
                        text = "$level NIVEL • $period ${plan.typePeriod}".uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                    )
                }
                items(items) { item ->
                    CourseRowItem(
                        item = item,
                        canEdit = canEdit,
                        onEdit = { courseToEdit = item.course },
                        onMarkPassed = { item.course.idCourse?.let { viewModel.markCourseAsPassed(it) } }
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }

    if (courseToEdit != null) {
        EditCourseDialog(
            course = courseToEdit!!,
            onDismiss = { courseToEdit = null },
            onSave = { updatedCourse ->
                viewModel.updateCourse(updatedCourse)
                courseToEdit = null
            }
        )
    }
}

@Composable
fun PlanHeaderCard(
    plan: StudyPlanDto, 
    coursesWithStatus: List<CourseWithStatus>,
    canEdit: Boolean,
    onEdit: () -> Unit
) {
    val totalCredits = coursesWithStatus.sumOf { it.course.numCredits }
    val passedCredits = coursesWithStatus.filter { it.studentCourse?.idStatus == 4 }.sumOf { it.course.numCredits }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.dscName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.dscCareer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canEdit) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("CRÉDITOS", "$passedCredits / $totalCredits")
                StatItem("AÑO", plan.yearLevel)
                StatItem("SISTEMA", plan.typePeriod)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun CourseRowItem(
    item: CourseWithStatus,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onMarkPassed: () -> Unit
) {
    val course = item.course
    val studentCourse = item.studentCourse
    val isPassed = studentCourse?.idStatus == 4
    val isLocked = studentCourse?.idStatus == 3

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isPassed) SinoPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isPassed) SinoPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isPassed) SinoPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant, 
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = when {
                            isPassed -> R.drawable.check
                            isLocked -> R.drawable.lock
                            else -> R.drawable.lock_open
                        }
                    ),
                    contentDescription = null,
                    tint = if (isPassed) SinoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.dscName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${course.dscCode} • ${course.numCredits} CR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canEdit) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun EditCourseDialog(
    course: CourseDto,
    onDismiss: () -> Unit,
    onSave: (CourseDto) -> Unit
) {
    var name by remember { mutableStateOf(course.dscName) }
    var code by remember { mutableStateOf(course.dscCode) }
    var credits by remember { mutableStateOf(course.numCredits.toString()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Editar Curso",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Curso") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = SinoPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = SinoPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = SinoPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = SinoPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    OutlinedTextField(
                        value = credits,
                        onValueChange = { if (it.all { char -> char.isDigit() }) credits = it },
                        label = { Text("Créditos") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = SinoPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = SinoPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SinoButton(
                        text = "Guardar Cambios",
                        onClick = {
                            onSave(
                                course.copy(
                                    dscName = name,
                                    dscCode = code,
                                    numCredits = credits.toIntOrNull() ?: 0
                                )
                            )
                        },
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }
    }
}