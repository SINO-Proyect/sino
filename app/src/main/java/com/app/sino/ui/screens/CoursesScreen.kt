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
import com.app.sino.ui.components.SinoTextField
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
                Toast.makeText(context, (updateState as Resource.Error).message ?: "Error", Toast.LENGTH_LONG).show()
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF050505))
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
                    Spacer(modifier = Modifier.height(24.dp))
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
                .size(120.dp)
                .background(SinoPrimary.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, SinoPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.book_open_duotone),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = SinoPrimary
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Sin plan de estudio",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Configura tu camino académico para empezar a seguir tu progreso.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        SinoButton(
            text = "Configurar Plan",
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
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp), // Padding normalizado
        verticalArrangement = Arrangement.spacedBy(12.dp) // Espaciado más ajustado
    ) {
        item {
            PlanHeaderCard(
                plan = plan, 
                coursesWithStatus = coursesWithStatus,
                canEdit = canEdit,
                onEdit = { Toast.makeText(context, "Edición próximamente", Toast.LENGTH_SHORT).show() }
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
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Black,
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.dscName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.dscCareer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                if (canEdit) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
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
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPassed) Color(0xFF0F0F0F) else Color(0xFF0A0A0A)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isPassed) SinoPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isPassed) SinoPrimary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f), 
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, if (isPassed) SinoPrimary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp)),
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
                    tint = if (isPassed) SinoPrimary else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.dscName.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${course.dscCode} • ${course.numCredits} CR",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            if (canEdit) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
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
        Card(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "EDITAR CURSO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                SinoTextField(
                    label = "Nombre",
                    value = name,
                    onValueChange = { name = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        SinoTextField(
                            label = "Código",
                            value = code,
                            onValueChange = { code = it }
                        )
                    }
                    Box(modifier = Modifier.weight(0.7f)) {
                        SinoTextField(
                            label = "Créditos",
                            value = credits,
                            onValueChange = { if (it.all { char -> char.isDigit() }) credits = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    SinoButton(
                        text = "GUARDAR",
                        onClick = {
                            onSave(
                                course.copy(
                                    dscName = name,
                                    dscCode = code,
                                    numCredits = credits.toIntOrNull() ?: 0
                                )
                            )
                        },
                        modifier = Modifier.width(140.dp)
                    )
                }
            }
        }
    }
}
