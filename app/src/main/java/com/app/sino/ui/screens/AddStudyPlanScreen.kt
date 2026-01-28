package com.app.sino.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.data.remote.dto.UniversityDto
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudyPlanScreen(
    onBack: () -> Unit,
    viewModel: AddStudyPlanViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(0) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is AddPlanUiState.Saved -> {
                Toast.makeText(context, "¡Plan creado exitosamente!", Toast.LENGTH_LONG).show()
                onBack()
            }
            is AddPlanUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is AddPlanUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }
    
    BackHandler {
        if (currentStep > 0) currentStep = 0 else onBack()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            if (currentStep == 0) "Crear Nuevo Plan" else "Malla Curricular",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        },
        bottomBar = {
            BottomActionBar(
                currentStep = currentStep,
                onCancel = { showCancelDialog = true },
                onNext = { 
                    if (viewModel.validateGeneralInfo()) {
                        currentStep = 1 
                    }
                },
                onBackStep = { currentStep = 0 },
                onSave = { viewModel.savePlan() }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
            
            if (currentStep == 0) {
                GeneralInfoStep(viewModel)
            } else {
                CurriculumStep(viewModel)
            }

            if (uiState is AddPlanUiState.Loading) {
                LoadingOverlay(message = (uiState as AddPlanUiState.Loading).message)
            }
            
            if (showCancelDialog) {
                Dialog(
                    onDismissRequest = { showCancelDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.9f).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("¿Cancelar creación?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Se perderán todos los datos ingresados hasta el momento.", style = MaterialTheme.typography.bodyMedium)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { showCancelDialog = false }) {
                                        Text("No, continuar")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { 
                                            showCancelDialog = false
                                            onBack() 
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Sí, cancelar")
                                    }
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
fun BottomActionBar(
    currentStep: Int,
    onCancel: () -> Unit,
    onNext: () -> Unit,
    onBackStep: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        tonalElevation = 16.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep == 0) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Siguiente", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onBackStep,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Atrás", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Finalizar Plan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GeneralInfoStep(viewModel: AddStudyPlanViewModel) {
    val planName by viewModel.planName.collectAsState()
    val careerName by viewModel.careerName.collectAsState()
    val yearLevel by viewModel.yearLevel.collectAsState()
    val periodType by viewModel.periodType.collectAsState()
    
    val universities by viewModel.universities.collectAsState()
    val selectedUniId by viewModel.selectedUniversityId.collectAsState()
    
    val selectedUniName = universities.find { it.idUniversity == selectedUniId }?.dscName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        
        HeaderSection(
            title = "Datos de tu Universidad",
            subtitle = "Selecciona dónde estudias para sincronizarte con otros estudiantes."
        )

        UniversitySelectorField(
            selectedName = selectedUniName,
            viewModel = viewModel
        )

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        HeaderSection(
            title = "Detalles de la Carrera",
            subtitle = "Personaliza el nombre y estructura de tu plan de estudios."
        )

        LabeledTextField(
            label = "Nombre del Plan",
            value = planName,
            onValueChange = { viewModel.planName.value = it },
            placeholder = "Ej. Ingeniería en Sistemas 2024",
            leadingIcon = { 
                Icon(
                    painter = painterResource(id = com.app.sino.R.drawable.notebook_duotone), 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                ) 
            }
        )

        LabeledTextField(
            label = "Carrera o Facultad",
            value = careerName,
            onValueChange = { viewModel.careerName.value = it },
            placeholder = "Ej. Facultad de Ingeniería"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                LabeledTextField(
                    label = "Año de Ingreso",
                    value = yearLevel,
                    onValueChange = { viewModel.yearLevel.value = it },
                    placeholder = "2024",
                    keyboardType = KeyboardType.Number
                )
            }
            
            Box(modifier = Modifier.weight(1f)) {
                 LabeledDropdown(
                     label = "Tipo de Periodo",
                     selected = periodType,
                     options = listOf("Semestre", "Cuatrimestre", "Trimestre", "Anual"),
                     onSelect = { viewModel.updatePeriodType(it) }
                 )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HeaderSection(title: String, subtitle: String) {
    Column {
        Text(
            title, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            subtitle, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = leadingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next)
        )
    }
}

@Composable
fun LabeledDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Box {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface
                )
            )
             Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UniversitySelectorField(
    selectedName: String?,
    viewModel: AddStudyPlanViewModel
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Universidad", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { showDialog = true }
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = com.app.sino.R.drawable.buildings_duotone),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedName != null) {
                        Text(selectedName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    } else {
                        Text("Seleccionar Universidad...", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showDialog) {
        UniversitySearchDialog(
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onSelect = { 
                viewModel.selectedUniversityId.value = it.idUniversity
                showDialog = false
            }
        )
    }
}

@Composable
fun UniversitySearchDialog(
    viewModel: AddStudyPlanViewModel,
    onDismiss: () -> Unit,
    onSelect: (UniversityDto) -> Unit
) {
    val universities by viewModel.universities.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Buscar Universidad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it 
                            viewModel.searchUniversities(it)
                        },
                        placeholder = { Text("Escribe para buscar...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (universities.isEmpty()) {
                             item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No encontramos resultados", color = Color.Gray)
                                    TextButton(onClick = { showCreateDialog = true }) {
                                        Text("Agregar Nueva Universidad")
                                    }
                                }
                            }
                        } else {
                            val groupedUnis = universities.groupBy { it.dscName.first().uppercaseChar() }.toSortedMap()
                            
                            groupedUnis.forEach { (initial, unis) ->
                                item {
                                    Text(
                                        text = initial.toString(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                                    )
                                }
                                
                                items(unis) { uni ->
                                    Card(
                                        onClick = { onSelect(uni) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(id = com.app.sino.R.drawable.buildings_duotone), 
                                                contentDescription = null, 
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(uni.dscName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                                uni.dscCountry?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            item {
                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                TextButton(
                                    onClick = { showCreateDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("¿No está tu universidad? Agregala aquí")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AddUniversityDialog(
            initialName = searchQuery,
            onDismiss = { showCreateDialog = false },
            onSave = { name, country ->
                viewModel.createAndSelectUniversity(name, country)
                showCreateDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun CurriculumStep(viewModel: AddStudyPlanViewModel) {
    val cycles by viewModel.cycles.collectAsState()
    val periodType by viewModel.periodType.collectAsState()
    
    var showCourseDialog by remember { mutableStateOf(false) }
    var showCycleDialog by remember { mutableStateOf(false) }
    var cycleToDelete by remember { mutableStateOf<String?>(null) }
    var selectedCycleIdForCourse by remember { mutableStateOf<String?>(null) }
    var courseToEdit by remember { mutableStateOf<LocalCourse?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
         LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp, top = 20.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                HeaderSection(
                    title = "Construye tu Camino",
                    subtitle = "Organiza tus periodos y agrega las materias correspondientes."
                )
            }

            items(cycles) { cycle ->
                CycleItemView(
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
                    onRemoveCourse = { viewModel.removeCourse(cycle.id, it) },
                    onRemoveCycle = { cycleToDelete = cycle.id }
                )
            }
            
            item {
                OutlinedButton(
                    onClick = { showCycleDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Nuevo Periodo ($periodType)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (cycleToDelete != null) {
        Dialog(
            onDismissRequest = { cycleToDelete = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("¿Eliminar periodo?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Se eliminarán todos los cursos dentro de este periodo. Esta acción no se puede deshacer.", style = MaterialTheme.typography.bodyMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { cycleToDelete = null }) {
                                Text("Cancelar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { 
                                    viewModel.removeCycle(cycleToDelete!!)
                                    cycleToDelete = null 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
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
        val currentCycle = cycles.find { it.id == selectedCycleIdForCourse }
        val availableCoreqs = currentCycle?.courses?.filter { it.tempId != courseToEdit?.tempId } ?: emptyList()

        AddCourseDialog(
            initialCourse = courseToEdit,
            availablePrerequisites = availablePrereqs,
            availableCorequisites = availableCoreqs,
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
fun CycleItemView(
    cycle: LocalCycle,
    onAddCourse: () -> Unit,
    onEditCourse: (LocalCourse) -> Unit,
    onRemoveCourse: (String) -> Unit,
    onRemoveCycle: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(cycle.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            IconButton(onClick = onRemoveCycle, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (cycle.courses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp).clickable { onAddCourse() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Agregar materias aquí", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    cycle.courses.forEachIndexed { index, course ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEditCourse(course) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    course.code,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(course.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${course.credits} créditos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            course.typeCourse, 
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { onRemoveCourse(course.tempId) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color.Gray)
                            }
                        }
                        if (index < cycle.courses.size - 1) Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddCourse() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+ Agregar Materia", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(message, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AddUniversityDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var country by remember { mutableStateOf("Costa Rica") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Agregar Nueva Universidad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la Universidad") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("País") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancelar") }
                        Button(
                            onClick = { onSave(name, country) },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Crear y Seleccionar") }
                    }
                }
            }
        }
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
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
                        label = { Text("Grado (ej. I, II)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text("Número (ej. 1, 2)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDismiss) { Text("Cancelar") }
                        Button(
                            onClick = { onSave(degree, number) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Agregar") }
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
    availableCorequisites: List<LocalCourse>,
    onDismiss: () -> Unit,
    onSave: (LocalCourse) -> Unit
) {
    var name by remember { mutableStateOf(initialCourse?.name ?: "") }
    var code by remember { mutableStateOf(initialCourse?.code ?: "") }
    var credits by remember { mutableStateOf(initialCourse?.credits?.toString() ?: "3") }
    var description by remember { mutableStateOf(initialCourse?.description ?: "") }
    var academicLevel by remember { mutableStateOf(initialCourse?.typeCourse ?: "Bachillerato") }
    
    var prereqSearch by remember { mutableStateOf("") }
    var coreqSearch by remember { mutableStateOf("") }
    
    var showError by remember { mutableStateOf(false) }
    
    val selectedPrereqs = remember { 
        mutableStateListOf<String>().apply { 
            initialCourse?.prerequisitesIds?.let { addAll(it) } 
        } 
    }
    val selectedCoreqs = remember {
        mutableStateListOf<String>().apply {
            initialCourse?.corequisitesIds?.let { addAll(it) }
        }
    }

    val filteredPrereqs = remember(prereqSearch, availablePrerequisites) {
        availablePrerequisites.filter { 
            it.name.contains(prereqSearch, ignoreCase = true) || 
            it.code.contains(prereqSearch, ignoreCase = true) 
        }
    }

    val filteredCoreqs = remember(coreqSearch, availableCorequisites) {
        availableCorequisites.filter { 
            it.name.contains(coreqSearch, ignoreCase = true) || 
            it.code.contains(coreqSearch, ignoreCase = true) 
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Column(modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp)) {
                        Text(
                            if (initialCourse == null) "Nuevo Curso" else "Editar Curso", 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (showError) {
                            Text(
                                "Campos marcados con * son obligatorios.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Basic Info
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nombre del Curso *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = showError && name.isBlank()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it },
                                label = { Text("Código *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                isError = showError && code.isBlank()
                            )
                            OutlinedTextField(
                                value = credits,
                                onValueChange = { if (it.all { char -> char.isDigit() }) credits = it },
                                label = { Text("Créditos *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(12.dp),
                                isError = showError && credits.isBlank()
                            )
                        }

                        // Academic Level Selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Nivel Académico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            val levels = listOf("Diplomado", "Bachillerato", "Licenciatura", "Maestría", "Doctorado")
                            
                            // Re-implementing FlowRow behavior manually or using a Box with wrapping if needed,
                            // but for simplicity and since we don't have ExperimentalLayoutApi working well,
                            // I'll use a simple Row with scroll or just a Column of Rows.
                            // Actually, I'll use a simple Row for now or just a few small Rows.
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    levels.take(3).forEach { level ->
                                        FilterChip(
                                            selected = academicLevel == level,
                                            onClick = { academicLevel = level },
                                            label = { Text(level) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    levels.drop(3).forEach { level ->
                                        FilterChip(
                                            selected = academicLevel == level,
                                            onClick = { academicLevel = level },
                                            label = { Text(level) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // PREREQUISITES
                        RequirementSearchList(
                            title = "Pre-requisitos",
                            searchQuery = prereqSearch,
                            onSearchChange = { prereqSearch = it },
                            items = filteredPrereqs,
                            selectedIds = selectedPrereqs,
                            onToggle = { id ->
                                if (selectedPrereqs.contains(id)) selectedPrereqs.remove(id)
                                else selectedPrereqs.add(id)
                            }
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // COREQUISITES
                        RequirementSearchList(
                            title = "Co-requisitos",
                            searchQuery = coreqSearch,
                            onSearchChange = { coreqSearch = it },
                            items = filteredCoreqs,
                            selectedIds = selectedCoreqs,
                            onToggle = { id ->
                                if (selectedCoreqs.contains(id)) selectedCoreqs.remove(id)
                                else selectedCoreqs.add(id)
                            }
                        )
                    }

                    // Fixed Footer
                                    Surface(
                                        tonalElevation = 2.dp,
                                        shadowElevation = 8.dp,
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 16.dp),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {                            OutlinedButton(
                                onClick = onDismiss,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp).weight(1f)
                            ) { 
                                Text("Cancelar", fontWeight = FontWeight.Bold) 
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { 
                                    if (name.isBlank() || code.isBlank() || credits.isBlank()) {
                                        showError = true
                                    } else {
                                        onSave(
                                            LocalCourse(
                                                tempId = initialCourse?.tempId ?: UUID.randomUUID().toString(),
                                                name = name,
                                                code = code,
                                                credits = credits.toIntOrNull() ?: 0,
                                                typeCourse = academicLevel,
                                                description = description,
                                                prerequisitesIds = selectedPrereqs.toList(),
                                                corequisitesIds = selectedCoreqs.toList()
                                            )
                                        ) 
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp).weight(1f)
                            ) { 
                                Text("Guardar", fontWeight = FontWeight.Bold) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequirementSearchList(
    title: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    items: List<LocalCourse>,
    selectedIds: List<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar por nombre o código...") },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        )

        if (items.isEmpty()) {
            Text("No se encontraron cursos disponibles.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.forEach { course ->
                    val isSelected = selectedIds.contains(course.tempId)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable { onToggle(course.tempId) }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(course.name, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            Text(course.code, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}