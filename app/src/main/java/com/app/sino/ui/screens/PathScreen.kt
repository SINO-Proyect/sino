package com.app.sino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.ui.components.CourseBadge
import com.app.sino.ui.model.BadgePalette
import com.app.sino.ui.model.CourseStatus
import com.app.sino.ui.model.PathTheme
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite

@Composable
fun PathScreen(
    onAddPlanClick: () -> Unit,
    viewModel: CoursesViewModel = viewModel()
) {
    val courses by viewModel.selectedPlanCourses.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Trigger load if empty
    LaunchedEffect(Unit) {
        if (courses.isEmpty()) {
            viewModel.loadStudyPlans()
        }
    }
    
    // Once plans are loaded, we need to ensure courses are loaded for the active plan
    val activePlan = (uiState as? CoursesUiState.Success)?.plans?.firstOrNull()
    LaunchedEffect(activePlan) {
        if (activePlan?.idStudyPlan != null && courses.isEmpty()) {
            viewModel.loadCoursesForPlan(activePlan.idStudyPlan)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SinoBlack)) {
        when (val state = uiState) {
            is CoursesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SinoWhite)
                }
            }
            is CoursesUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is CoursesUiState.Success -> {
                if (state.plans.isEmpty()) {
                    EmptyPlanState(onAddPlanClick)
                } else if (courses.isEmpty()) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SinoWhite)
                    }
                } else {
                    // Group by Period. We assume period is a string "I Cuatrimestre" etc.
                    // We try to parse an index from it or just use unique periods
                    val grouped = remember(courses) { 
                        courses.groupBy { it.dscPeriod } 
                    }
                    
                    // Sort keys? If they contain numbers "1", "2", etc.
                    val sortedKeys = grouped.keys.sortedBy { key ->
                        // Try to extract first number
                        key.filter { it.isDigit() }.toIntOrNull() ?: 999
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        sortedKeys.forEachIndexed { cycleIndex, periodName ->
                            val cycleCourses = grouped[periodName] ?: emptyList()
                            
                            item(span = { GridItemSpan(2) }) {
                                CycleHeader(periodName, cycleIndex)
                            }

                            items(cycleCourses.size, span = { index ->
                                // Center single item in last row logic
                                if (index == cycleCourses.size - 1 && cycleCourses.size % 2 != 0) GridItemSpan(2) else GridItemSpan(1)
                            }) { index ->
                                val course = cycleCourses[index]
                                
                                // Mock Status Logic (Simulating "Path")
                                // First period: Passed/Progress. Later: Locked.
                                val status = when {
                                    cycleIndex < 1 -> CourseStatus.PASSED
                                    cycleIndex == 1 -> if (index % 2 == 0) CourseStatus.IN_PROGRESS else CourseStatus.AVAILABLE
                                    else -> CourseStatus.LOCKED
                                }
                                
                                val palette = if (status == CourseStatus.LOCKED || status == CourseStatus.AVAILABLE) 
                                    PathTheme.grayPalette 
                                else 
                                    PathTheme.getBadgePalette(cycleIndex)

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    CourseBadge(
                                        courseName = course.dscName,
                                        courseCode = course.dscCode,
                                        status = status,
                                        palette = palette,
                                        requirementsCount = course.prerequisites?.size ?: 0
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CycleHeader(periodName: String, index: Int) {
    val palette = PathTheme.getBadgePalette(index)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = palette.main.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                )
                .border(
                    width = 1.dp,
                    color = palette.main.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = periodName.uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.sub4
                )
            )
        }
    }
}