package com.app.sino.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.components.SinoTextField
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudyPlanScreen(
    onBack: () -> Unit,
    viewModel: AddStudyPlanViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Estados de navegación interna del wizard
    var currentStep by remember { mutableIntStateOf(0) } // 0: Datos, 1: Malla

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onPdfSelected(it, context) }
    }

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is AddPlanUiState.Saved -> {
                Toast.makeText(context, "¡Plan de Estudios Guardado!", Toast.LENGTH_LONG).show()
                onBack()
            }
            is AddPlanUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is AddPlanUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Plan de Estudios", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.caret_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Barra de navegación entre pasos
            BottomStepBar(
                currentStep = currentStep,
                onNext = { if (currentStep == 0) currentStep = 1 else viewModel.savePlan() },
                onPrev = { if (currentStep == 1) currentStep = 0 },
                isLastStep = currentStep == 1
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            
            if (currentStep == 0) {
                GeneralInfoStep(viewModel)
            } else {
                CurriculumStep(viewModel)
            }

            // Loading Overlay
            if (uiState is AddPlanUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            (uiState as AddPlanUiState.Loading).message, 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomStepBar(currentStep: Int, onNext: () -> Unit, onPrev: () -> Unit, isLastStep: Boolean) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                OutlinedButton(onClick = onPrev) {
                    Text("Atrás")
                }
            } else {
                Spacer(modifier = Modifier.width(10.dp))
            }

            Button(onClick = onNext) {
                Text(if (isLastStep) "Guardar Plan" else "Siguiente")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralInfoStep(viewModel: AddStudyPlanViewModel) {
    val universities by viewModel.universities.collectAsState()
    val planName by viewModel.planName.collectAsState()
    val careerName by viewModel.careerName.collectAsState()
    val yearLevel by viewModel.yearLevel.collectAsState()
    val selectedUniId by viewModel.selectedUniversityId.collectAsState()
    val periodType by viewModel.periodType.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Datos Generales", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        // University Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Universidad", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            var expanded by remember { mutableStateOf(false) }
            val selectedUniName = universities.find { it.idUniversity == selectedUniId }?.dscName ?: "Selecciona Universidad"
            
            Box {
                OutlinedCard(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedUniName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = expanded, 
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    universities.forEach { uni ->
                        DropdownMenuItem(
                            text = { Text(uni.dscName) },
                            onClick = {
                                viewModel.selectedUniversityId.value = uni.idUniversity
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        SinoTextField(
            value = planName,
            onValueChange = { viewModel.planName.value = it },
            label = "Nombre del Plan",
            placeholder = "ej. Bachillerato en Ingeniería 2024"
        )

        SinoTextField(
            value = careerName,
            onValueChange = { viewModel.careerName.value = it },
            label = "Carrera / Facultad",
            placeholder = "ej. Ingeniería en Sistemas"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SinoTextField(
                    value = yearLevel,
                    onValueChange = { viewModel.yearLevel.value = it },
                    label = "Año / Nivel",
                    placeholder = "ej. 2024"
                )
            }
            
            // Period Type Selector
            Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo de Periodo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                var expanded by remember { mutableStateOf(false) }
                val types = listOf("Semestre", "Cuatrimestre", "Trimestre", "Anual")
                
                Box {
                    OutlinedCard(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(periodType)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    viewModel.updatePeriodType(type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurriculumStep(viewModel: AddStudyPlanViewModel) {
    val cycles by viewModel.cycles.collectAsState()
    val periodType by viewModel.periodType.collectAsState()
    var showCourseDialog by remember { mutableStateOf(false) }
    var showCycleDialog by remember { mutableStateOf(false) }
    var selectedCycleIdForCourse by remember { mutableStateOf<String?>(null) }
    var courseToEdit by remember { mutableStateOf<LocalCourse?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${cycles.sumOf { it.courses.size }} cursos en total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            items(cycles) { cycle ->
                CycleItem(
                    cycle = cycle,
                    onAddCourse = {
                        courseToEdit = null
                        selectedCycleIdForCourse = cycle.id
                        showCourseDialog = true
                    },
                    onEditCourse = { course ->
                        courseToEdit = course
                        selectedCycleIdForCourse = cycle.id
                        showCourseDialog = true
                    },
                    onRemoveCourse = { courseId -> viewModel.removeCourse(cycle.id, courseId) },
                    onRemoveCycle = { viewModel.removeCycle(cycle.id) }
                )
            }

            item {
                Button(
                    onClick = { showCycleDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar $periodType")
                }
            }
        }
    }

    if (showCycleDialog) {
        AddCycleDialog(
            periodTypeName = periodType,
            onDismiss = { showCycleDialog = false },
            onSave = { degree, number ->
                viewModel.addCycle(degree, number)
                showCycleDialog = false
            }
        )
    }

    if (showCourseDialog && selectedCycleIdForCourse != null) {
        val availablePrereqs = viewModel.getAvailablePrerequisites(selectedCycleIdForCourse!!)
        AddCourseDialog(
            initialCourse = courseToEdit,
            availablePrerequisites = availablePrereqs,
            onDismiss = { 
                showCourseDialog = false
                courseToEdit = null
            },
            onSave = { course ->
                if (courseToEdit != null) {
                    viewModel.updateCourse(selectedCycleIdForCourse!!, course)
                } else {
                    viewModel.addCourseToCycle(selectedCycleIdForCourse!!, course)
                }
                showCourseDialog = false
                courseToEdit = null
            }
        )
    }
}

@Composable
fun AddCycleDialog(
    periodTypeName: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var degree by remember { mutableStateOf("I") }
    var number by remember { mutableStateOf("1") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Nuevo $periodTypeName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Nivel / Grado (ej. I, II, III)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("ej. I") }
                )

                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Número de $periodTypeName") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("ej. 1") }
                )

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onSave(degree, number) },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Agregar") }
                }
            }
        }
    }
}

@Composable
fun CycleItem(
    cycle: LocalCycle,
    onAddCourse: () -> Unit,
    onEditCourse: (LocalCourse) -> Unit,
    onRemoveCourse: (String) -> Unit,
    onRemoveCycle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cycle.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onAddCourse) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Curso", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onRemoveCycle) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Periodo", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            if (cycle.courses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin cursos en este periodo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                cycle.courses.forEach { course ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditCourse(course) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(course.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                if (course.code.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            course.code, 
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                            Text(
                                "${course.credits} Créditos • ${if(course.isObligatory) "Obligatorio" else "Electivo"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (course.prerequisitesIds.isNotEmpty()) {
                                Text(
                                    "Requisitos: ${course.prerequisitesIds.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = { onRemoveCourse(course.tempId) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Gray)
                        }
                    }
                    if (cycle.courses.last() != course) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseDialog(
    initialCourse: LocalCourse? = null,
    availablePrerequisites: List<LocalCourse>,
    onDismiss: () -> Unit,
    onSave: (LocalCourse) -> Unit
) {
    var name by remember { mutableStateOf(initialCourse?.name ?: "") }
    var code by remember { mutableStateOf(initialCourse?.code ?: "") }
    var credits by remember { mutableStateOf(initialCourse?.credits?.toString() ?: "3") }
    var isObligatory by remember { mutableStateOf(initialCourse?.isObligatory ?: true) }
    var description by remember { mutableStateOf(initialCourse?.description ?: "") }
    
    val selectedPrereqs = remember { 
        mutableStateListOf<String>().apply { 
            initialCourse?.prerequisitesIds?.let { addAll(it) } 
        } 
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth() // Usa todo el ancho disponible del diálogo estándar
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp) // El padding de afuera es el padding de los campos internos
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (initialCourse == null) "Nuevo Curso" else "Editar Curso", 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Curso") },
                    placeholder = { Text("ej. Programación I") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código") },
                        placeholder = { Text("ej. EIF200") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = credits,
                        onValueChange = { if (it.all { char -> char.isDigit() }) credits = it },
                        label = { Text("Créditos") },
                        placeholder = { Text("3") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Breve descripción del curso...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isObligatory = !isObligatory }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = isObligatory, onCheckedChange = { isObligatory = it })
                    Text("Es Curso Obligatorio", style = MaterialTheme.typography.bodyLarge)
                }

                // Sección Prerrequisitos
                Text("Cursos Requisito", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                if (availablePrerequisites.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            availablePrerequisites.forEach { prereq ->
                                val isSelected = selectedPrereqs.contains(prereq.tempId)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (isSelected) selectedPrereqs.remove(prereq.tempId)
                                            else selectedPrereqs.add(prereq.tempId)
                                        }
                                        .padding(8.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(prereq.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(prereq.code, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "No hay cursos en periodos anteriores para seleccionar como requisitos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { 
                            onSave(
                                LocalCourse(
                                    tempId = initialCourse?.tempId ?: UUID.randomUUID().toString(),
                                    name = name,
                                    code = code,
                                    credits = credits.toIntOrNull() ?: 0,
                                    isObligatory = isObligatory,
                                    description = description,
                                    prerequisitesIds = selectedPrereqs.toList()
                                )
                            ) 
                        },
                        enabled = name.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) { 
                        Text(if (initialCourse == null) "Agregar Curso" else "Guardar Cambios") 
                    }
                }
            }
        }
    }
}
