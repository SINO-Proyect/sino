package com.app.sino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.ui.theme.PremiumGradientEnd
import com.app.sino.ui.theme.PremiumGradientStart
import com.app.sino.ui.theme.SinoPrimary

@Composable
fun HomeScreen(
    viewModel: CoursesViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userData by profileViewModel.userState.collectAsState()
    val coursesWithStatus by viewModel.coursesWithStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState !is CoursesUiState.Success) {
            viewModel.loadStudyPlans()
        }
    }

    val activePlan = (uiState as? CoursesUiState.Success)?.plans?.firstOrNull()
    LaunchedEffect(activePlan) {
        if (activePlan?.idStudyPlan != null && coursesWithStatus.isEmpty()) {
            viewModel.loadCoursesForPlan(activePlan.idStudyPlan)
        }
    }
    
    val isLoading = uiState is CoursesUiState.Loading || (activePlan != null && coursesWithStatus.isEmpty())
    
    val inProgressCourses = coursesWithStatus.filter { it.studentCourse?.idStatus == 2 }
    val totalCredits = coursesWithStatus.sumOf { it.course.numCredits }
    val passedCredits = coursesWithStatus.filter { it.studentCourse?.idStatus == 4 }.sumOf { it.course.numCredits }
    val progress = if (totalCredits > 0) passedCredits.toFloat() / totalCredits else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Deep black background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (userData == null) "Hola..." else "¡Hola, ${userData?.fullName?.split(" ")?.firstOrNull()}!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tu progreso hoy es increíble.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Surface(
                        onClick = { /* TODO */ },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Main Progress Card (Apple Pro Style)
            item {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(32.dp)))
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            Brush.verticalGradient(listOf(PremiumGradientStart, PremiumGradientEnd)),
                                            RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Star, null, tint = Color.Black, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("PROGRESO ACADÉMICO", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                                    Text(activePlan?.dscName ?: "Sin plan activo", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = SinoPrimary,
                                trackColor = Color.White.copy(alpha = 0.05f)
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Column {
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = SinoPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Completado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "$passedCredits / $totalCredits CR",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Current Courses Section Header
            item {
                Text(
                    text = "CURSANDO",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            if (isLoading) {
                items(2) {
                    Box(modifier = Modifier.fillMaxWidth().height(90.dp).background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(24.dp)))
                }
            } else if (inProgressCourses.isEmpty()) {
                item {
                    Text(
                        "No tienes materias en curso.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        color = Color.White.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(inProgressCourses) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.book_open_duotone), 
                                    null, 
                                    tint = SinoPrimary, 
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.course.dscName.uppercase(), 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color.White, 
                                    style = MaterialTheme.typography.bodyMedium,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    item.course.dscCode, 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                            Text(
                                "${item.course.numCredits} CR", 
                                fontWeight = FontWeight.Bold, 
                                color = SinoPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
