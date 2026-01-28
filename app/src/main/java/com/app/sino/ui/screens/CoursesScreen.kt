package com.app.sino.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.data.remote.dto.StudyPlanDto
import com.app.sino.data.util.Resource
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite

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
                Toast.makeText(context, "Course updated successfully", Toast.LENGTH_SHORT).show()
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
        .background(SinoBlack)
    ) {
        when (val state = uiState) {
            is CoursesUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = SinoWhite
                )
            }
            is CoursesUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadStudyPlans() }) {
                        Text("Retry")
                    }
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
        Icon(
            painter = painterResource(id = R.drawable.book_open_duotone),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = SinoWhite.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Study Plan Configured",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SinoWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "It looks like you haven't set up your academic path yet. Create a study plan to start tracking your progress.",
            style = MaterialTheme.typography.bodyMedium,
            color = SinoWhite.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddPlanClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SinoWhite, contentColor = SinoBlack),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Configure Study Plan", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActivePlanView(plan: StudyPlanDto, viewModel: CoursesViewModel) {
    val courses by viewModel.selectedPlanCourses.collectAsState()
    val context = LocalContext.current
    var courseToEdit by remember { mutableStateOf<CourseDto?>(null) }

    // Load courses if not loaded
    LaunchedEffect(plan.idStudyPlan) {
        if (courses.isEmpty() && plan.idStudyPlan != null) {
            viewModel.loadCoursesForPlan(plan.idStudyPlan)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card
        item {
            PlanHeaderCard(
                plan = plan, 
                courses = courses,
                onEdit = { Toast.makeText(context, "Plan Edit coming soon", Toast.LENGTH_SHORT).show() }
            )
        }

        if (courses.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SinoWhite)
                }
            }
        } else {
            val grouped = courses.groupBy { it.dscPeriod }
            grouped.forEach { (period, periodCourses) ->
                item {
                    Text(
                        text = "PERIOD $period",
                        style = MaterialTheme.typography.labelSmall,
                        color = SinoWhite.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }
                items(periodCourses) { course ->
                    CourseRowItem(
                        course = course,
                        onEdit = { courseToEdit = course }
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
    courses: List<CourseDto>,
    onEdit: () -> Unit
) {
    val totalCredits = courses.sumOf { it.numCredits }
    val passedCredits = 0 
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2C2C2C), Color(0xFF121212)),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            )
            .border(1.dp, SinoWhite.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
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
                        fontWeight = FontWeight.Bold,
                        color = SinoWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.dscCareer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SinoWhite.copy(alpha = 0.7f)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SinoWhite.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Edit", tint = SinoWhite, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("CREDITS", "$passedCredits / $totalCredits")
                StatItem("LEVEL", plan.yearLevel)
                StatItem("PERIOD", plan.typePeriod)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SinoWhite.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.titleMedium, color = SinoWhite, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CourseRowItem(
    course: CourseDto,
    onEdit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SinoWhite.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        border = androidx.compose.foundation.BorderStroke(1.dp, SinoWhite.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Course Code Badge
                Surface(
                    color = if (course.typeCourse == "OBLIGATORY") Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFF1565C0).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        if (course.typeCourse == "OBLIGATORY") Color(0xFF2E7D32).copy(alpha = 0.5f) else Color(0xFF1565C0).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = course.dscCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = SinoWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.dscName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SinoWhite,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${course.numCredits} Credits",
                        style = MaterialTheme.typography.bodySmall,
                        color = SinoWhite.copy(alpha = 0.5f)
                    )
                }
                
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SinoWhite.copy(alpha = 0.5f))
                }
            }

            if (!course.prerequisites.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SinoWhite.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_path_outline),
                        contentDescription = null,
                        tint = SinoWhite.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Req: " + course.prerequisites.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = SinoWhite.copy(alpha = 0.5f)
                    )
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SinoWhite.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Edit Course",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SinoWhite
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SinoWhite,
                        unfocusedTextColor = SinoWhite,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = SinoWhite.copy(alpha = 0.3f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = SinoWhite.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SinoWhite,
                            unfocusedTextColor = SinoWhite,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = SinoWhite.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = SinoWhite.copy(alpha = 0.5f)
                        )
                    )
                    OutlinedTextField(
                        value = credits,
                        onValueChange = { if (it.all { char -> char.isDigit() }) credits = it },
                        label = { Text("Credits") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SinoWhite,
                            unfocusedTextColor = SinoWhite,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = SinoWhite.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = SinoWhite.copy(alpha = 0.5f)
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                course.copy(
                                    dscName = name,
                                    dscCode = code,
                                    numCredits = credits.toIntOrNull() ?: 0
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
