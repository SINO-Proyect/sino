package com.app.sino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.data.remote.dto.StudyPlanDto
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    onAddPlanClick: () -> Unit,
    viewModel: CoursesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStudyPlans()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Study Plan", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SinoBlack,
                    titleContentColor = SinoWhite,
                    actionIconContentColor = SinoWhite
                ),
                actions = {
                    if (uiState is CoursesUiState.Success && (uiState as CoursesUiState.Success).plans.isNotEmpty()) {
                        IconButton(onClick = { /* TODO: Navigate to Edit Plan Settings */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
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
            imageVector = Icons.Default.Info,
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
            colors = ButtonDefaults.buttonColors(containerColor = SinoWhite, contentColor = SinoBlack),
            shape = RoundedCornerShape(12.dp),
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

    // Load courses if not loaded
    LaunchedEffect(plan.idStudyPlan) {
        if (courses.isEmpty() && plan.idStudyPlan != null) {
            viewModel.loadCoursesForPlan(plan.idStudyPlan)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            PlanHeaderCard(plan, courses)
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
                        style = MaterialTheme.typography.labelMedium,
                        color = SinoWhite.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
                    )
                }
                items(periodCourses) { course ->
                    CourseRowItem(course)
                }
            }
        }
    }
}

@Composable
fun PlanHeaderCard(plan: StudyPlanDto, courses: List<CourseDto>) {
    val totalCredits = courses.sumOf { it.numCredits }

    val passedCredits = 0 
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = plan.dscName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SinoWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plan.dscCareer,
                style = MaterialTheme.typography.bodyLarge,
                color = SinoWhite.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("CREDITS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("$passedCredits / $totalCredits", style = MaterialTheme.typography.titleMedium, color = SinoWhite, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("LEVEL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(plan.yearLevel, style = MaterialTheme.typography.titleMedium, color = SinoWhite, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("PERIOD", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(plan.typePeriod, style = MaterialTheme.typography.titleMedium, color = SinoWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CourseRowItem(course: CourseDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { /* Open course details */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (course.typeCourse == "OBLIGATORY") Color(0xFF2E7D32) // Green
                        else Color(0xFF1565C0) // Blue
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = course.dscCode.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.dscName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SinoWhite
                )
                Text(
                    text = "${course.numCredits} Credits • ${course.dscCode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            IconButton(onClick = { }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}