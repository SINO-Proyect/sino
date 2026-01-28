package com.app.sino.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.data.remote.dto.CourseDto
import com.app.sino.ui.components.CourseBadge
import com.app.sino.ui.model.BadgePalette
import com.app.sino.ui.model.CourseStatus
import com.app.sino.ui.model.PathTheme
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite
import com.app.sino.ui.util.romanToDecimal
import com.app.sino.data.util.Resource

@Composable
fun PathScreen(
    onAddPlanClick: () -> Unit,
    viewModel: CoursesViewModel = viewModel()
) {
    val coursesWithStatus by viewModel.coursesWithStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.courseUpdateState.collectAsState()
    var selectedItemForAction by remember { mutableStateOf<CourseWithStatus?>(null) }
    
    LaunchedEffect(Unit) {
        if (coursesWithStatus.isEmpty()) {
            viewModel.loadStudyPlans()
        }
    }
    
    val activePlan = (uiState as? CoursesUiState.Success)?.plans?.firstOrNull()
    LaunchedEffect(activePlan) {
        if (activePlan?.idStudyPlan != null && coursesWithStatus.isEmpty()) {
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
                ErrorView(state.message) { viewModel.loadStudyPlans() }
            }
            is CoursesUiState.Success -> {
                if (state.plans.isEmpty()) {
                    EmptyPlanState(onAddPlanClick)
                } else if (coursesWithStatus.isEmpty()) {
                     LoadingCoursesView()
                } else {
                    val grouped = remember(coursesWithStatus) { 
                        coursesWithStatus.groupBy { "${it.course.dscLevel}|${it.course.dscPeriod}" } 
                    }
                    
                    val sortedKeys = grouped.keys.sortedWith(compareBy({ key ->
                        key.split("|")[0].romanToDecimal()
                    }, { key ->
                        val period = key.split("|")[1]
                        period.filter { it.isDigit() }.toIntOrNull() ?: 999
                    }))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp, top = 0.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        reverseLayout = false
                    ) {
                        sortedKeys.forEachIndexed { cycleIndex, compositeKey ->
                            val cycleItems = grouped[compositeKey] ?: emptyList()
                            val parts = compositeKey.split("|")
                            val displayHeader = "${parts[0]} NIVEL - ${parts[1]} ${activePlan?.typePeriod ?: "Periodo"}"
                            
                            // 1. HEADER AT TOP
                            item(span = { GridItemSpan(2) }) {
                                CycleHeader(displayHeader, cycleIndex)
                            }

                            // 2. BADGES FOLLOWING HEADER
                            items(cycleItems.size, span = { index ->
                                if (index == cycleItems.size - 1 && cycleItems.size % 2 != 0) GridItemSpan(2) else GridItemSpan(1)
                            }) { index ->
                                val item = cycleItems[index]
                                val status = getStatus(item.studentCourse?.idStatus)
                                val isUpdating = updateState is Resource.Loading && selectedItemForAction?.course?.idCourse == item.course.idCourse

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(enabled = updateState !is Resource.Loading) { 
                                            selectedItemForAction = item 
                                        },
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    CourseBadge(
                                        courseName = item.course.dscName,
                                        courseCode = item.course.dscCode,
                                        status = status,
                                        palette = if (status == CourseStatus.LOCKED) PathTheme.grayPalette else PathTheme.getBadgePalette(cycleIndex),
                                        prerequisitesCodes = item.course.prerequisites ?: emptyList()
                                    )
                                    
                                    if (isUpdating) {
                                        Box(
                                            modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp, color = Color(0xFF50C878))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedItemForAction != null) {
            CourseActionDialog(
                item = selectedItemForAction!!,
                onDismiss = { selectedItemForAction = null },
                onAction = { statusId ->
                    viewModel.updateCourseStatus(selectedItemForAction!!.course.idCourse!!, statusId)
                    selectedItemForAction = null
                }
            )
        }
    }
}

@Composable
fun CourseActionDialog(
    item: CourseWithStatus,
    onDismiss: () -> Unit,
    onAction: (Int) -> Unit
) {
    val statusId = item.studentCourse?.idStatus ?: 3
    val isLocked = statusId == 3
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121416)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFF50C878).copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.book_open_duotone),
                        contentDescription = null,
                        tint = Color(0xFF50C878),
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    item.course.dscName.uppercase(), 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Black, 
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    "CÓDIGO: ${item.course.dscCode}", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (isLocked) {
                    StatusWarning("MATERIA BLOQUEADA\nAprueba los requisitos para habilitarla.")
                    if (!item.course.prerequisites.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("REQUISITOS FALTANTES:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.course.prerequisites!!.forEach { req ->
                                Surface(
                                    color = Color(0xFFEF5350).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFEF5350).copy(alpha = 0.3f))
                                ) {
                                    Text(text = req, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFEF5350), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                } else {
                    if (statusId != 4) {
                        HoldToConfirmButton(
                            text = "MANTENER PARA GANAR",
                            color = Color(0xFF2E7D32),
                            onConfirmed = { onAction(4) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (statusId != 2) {
                            HoldToConfirmButton(
                                text = "MANTENER PARA CURSAR",
                                color = Color(0xFF43A047),
                                onConfirmed = { onAction(2) }
                            )
                        } else {
                            ActionButton(
                                text = "QUITAR DE CURSANDO", 
                                icon = R.drawable.lock_open,
                                color = Color.White.copy(alpha = 0.6f),
                                onClick = { onAction(1) }
                            )
                        }
                    } else {
                        StatusSuccess("HAS GANADO ESTA MATERIA")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("CERRAR", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatusSuccess(message: String) {
    Surface(
        color = Color(0xFF2E7D32).copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(painter = painterResource(id = R.drawable.check), contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HoldToConfirmButton(
    text: String,
    color: Color,
    onConfirmed: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    var isPressing by remember { mutableStateOf(false) }

    LaunchedEffect(isPressing) {
        if (isPressing) {
            val startTime = System.currentTimeMillis()
            while (isPressing && progress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed / 1500f).coerceAtMost(1f)
                kotlinx.coroutines.delay(16)
            }
            if (progress >= 1f) {
                onConfirmed()
            } else {
                progress = 0f
            }
        } else {
            progress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressing = true
                        tryAwaitRelease()
                        isPressing = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .background(color.copy(alpha = 0.4f))
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.check), contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: Int,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Icon(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun StatusWarning(message: String) {
    Surface(
        color = Color(0xFFEF5350).copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.lock), contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = Color(0xFFEF5350), style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun getStatus(id: Int?): CourseStatus = when(id) {
    1 -> CourseStatus.AVAILABLE
    2 -> CourseStatus.IN_PROGRESS
    3 -> CourseStatus.LOCKED
    4 -> CourseStatus.PASSED
    else -> CourseStatus.LOCKED
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Error: $message", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
fun LoadingCoursesView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = SinoWhite)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando cursos del plan...", color = SinoWhite)
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