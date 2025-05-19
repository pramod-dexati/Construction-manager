package com.km.expense.screens.equipment

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.km.expense.data.api.ApiClient
import com.km.expense.data.api.models.*
import com.km.expense.data.preferences.UserPreferences
import com.km.expense.navigation.AppNavigationActions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EquipmentTrackingScreen(navigationActions: AppNavigationActions) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences() }
    val apiService = remember { ApiClient.apiService }
    val userId = remember { userPreferences.getUserId() }
    
    var isLoading by remember { mutableStateOf(true) }
    var equipment by remember { mutableStateOf<List<Equipment>>(emptyList()) }
    var workers by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var equipmentAssignments by remember { mutableStateOf<List<EquipmentAssignment>>(emptyList()) }
    var showAddEquipmentDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showCheckinDialog by remember { mutableStateOf(false) }
    var showUpdateStatusDialog by remember { mutableStateOf(false) }
    var selectedEquipment by remember { mutableStateOf<Equipment?>(null) }
    var filterStatus by remember { mutableStateOf("") }
    
    val scaffoldState = rememberScaffoldState()
    
    LaunchedEffect(key1 = userId) {
        if (userId.isNotEmpty()) {
            try {
                // Load equipment
                val equipmentResponse = apiService.getEquipment("d5079fe5-e81c-454d-a170-8530331d8833", "equipment")
                equipment = equipmentResponse
                
                // Load workers
                val workersResponse = apiService.getWorkers("d5079fe5-e81c-454d-a170-8530331d8833", "workers", userId)
                workers = workersResponse
                
                // Load equipment assignments
                val assignments = mutableListOf<EquipmentAssignment>()
                for (equip in equipment) {
                    try {
                        val equipmentAssignmentsResponse = apiService.getEquipmentAssignments(
                            "d5079fe5-e81c-454d-a170-8530331d8833",
                            "equipment_assignments",
                            equip.id,
                            null
                        )
                        assignments.addAll(equipmentAssignmentsResponse)
                    } catch (e: Exception) {
                        Log.e("EquipmentTracking", "Error loading assignments for equipment ${equip.id}: ${e.message}")
                    }
                }
                equipmentAssignments = assignments
            } catch (e: Exception) {
                Log.e("EquipmentTracking", "Error loading data: ${e.message}")
                Toast.makeText(context, "Error loading equipment data", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Equipment Tracking") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { navigationActions.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddEquipmentDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Equipment")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEquipmentDialog = true },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Equipment", tint = Color.White)
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Filter options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter by status:",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    
                    FilterChip(
                        selected = filterStatus.isEmpty(),
                        onClick = { filterStatus = "" },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text("All")
                    }
                    
                    FilterChip(
                        selected = filterStatus == "available",
                        onClick = { filterStatus = "available" },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text("Available")
                    }
                    
                    FilterChip(
                        selected = filterStatus == "in_use",
                        onClick = { filterStatus = "in_use" },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text("In Use")
                    }
                    
                    FilterChip(
                        selected = filterStatus == "maintenance",
                        onClick = { filterStatus = "maintenance" }
                    ) {
                        Text("Maintenance")
                    }
                }
                
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (equipment.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "No Equipment",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No equipment added yet",
                                fontSize = 18.sp,
                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddEquipmentDialog = true },
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                            ) {
                                Text("Add Equipment", color = Color.White)
                            }
                        }
                    }
                } else {
                    val filteredEquipment = if (filterStatus.isEmpty()) {
                        equipment
                    } else {
                        equipment.filter { it.status == filterStatus }
                    }
                    
                    if (filteredEquipment.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No equipment matches the selected filter")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredEquipment) { equip ->
                                val currentAssignment = getCurrentAssignment(equip.id, equipmentAssignments)
                                val assignedWorker = if (currentAssignment != null) {
                                    workers.find { it.id == currentAssignment.workerId }
                                } else null
                                
                                EquipmentItem(
                                    equipment = equip,
                                    assignedWorker = assignedWorker,
                                    onCheckout = {
                                        selectedEquipment = equip
                                        showCheckoutDialog = true
                                    },
                                    onCheckin = {
                                        selectedEquipment = equip
                                        showCheckinDialog = true
                                    },
                                    onUpdateStatus = {
                                        selectedEquipment = equip
                                        showUpdateStatusDialog = true
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp)) // For FAB space
                            }
                        }
                    }
                }
            }
        }
    )
    
    if (showAddEquipmentDialog) {
        AddEquipmentDialog(
            onDismiss = { showAddEquipmentDialog = false },
            onAddEquipment = { name, type, condition ->
                coroutineScope.launch {
                    try {
                        val now = Date()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                        val timestamp = dateFormat.format(now)
                        
                        val equipmentRequest = CreateEquipmentRequest(
                            appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                            tableName = "equipment",
                            data = CreateEquipmentRequest.EquipmentData(
                                name = name,
                                type = type,
                                status = "available",
                                condition = condition,
                                lastMaintenance = timestamp
                            )
                        )
                        val response = apiService.createEquipment(equipmentRequest)
                        Log.d("EquipmentTracking", "Equipment created: $response")
                        
                        // Refresh equipment list
                        val equipmentResponse = apiService.getEquipment("d5079fe5-e81c-454d-a170-8530331d8833", "equipment")
                        equipment = equipmentResponse
                        
                        Toast.makeText(context, "Equipment added successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("EquipmentTracking", "Error creating equipment: ${e.message}")
                        Toast.makeText(context, "Error adding equipment: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                showAddEquipmentDialog = false
            }
        )
    }
    
    if (showCheckoutDialog && selectedEquipment != null) {
        CheckoutEquipmentDialog(
            equipment = selectedEquipment!!,
            workers = workers.filter { it.isActive },
            onDismiss = { showCheckoutDialog = false },
            onCheckout = { workerId ->
                coroutineScope.launch {
                    try {
                        val now = Date()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                        val timestamp = dateFormat.format(now)
                        
                        // Create equipment assignment
                        val assignmentRequest = CreateEquipmentAssignmentRequest(
                            appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                            tableName = "equipment_assignments",
                            data = CreateEquipmentAssignmentRequest.EquipmentAssignmentData(
                                equipmentId = selectedEquipment!!.id,
                                workerId = workerId,
                                checkedOut = timestamp,
                                checkedIn = null
                            )
                        )
                        apiService.createEquipmentAssignment(assignmentRequest)
                        
                        // Update equipment status
                        val updateEquipmentRequest = UpdateEquipmentRequest(
                            appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                            tableName = "equipment",
                            data = UpdateEquipmentRequest.EquipmentData(
                                id = selectedEquipment!!.id,
                                name = selectedEquipment!!.name,
                                type = selectedEquipment!!.type,
                                status = "in_use",
                                condition = selectedEquipment!!.condition,
                                lastMaintenance = selectedEquipment!!.lastMaintenance
                            )
                        )
                        apiService.updateEquipment(updateEquipmentRequest)
                        
                        // Refresh data
                        val equipmentResponse = apiService.getEquipment("d5079fe5-e81c-454d-a170-8530331d8833", "equipment")
                        equipment = equipmentResponse
                        
                        val assignments = mutableListOf<EquipmentAssignment>()
                        for (equip in equipment) {
                            try {
                                val equipmentAssignmentsResponse = apiService.getEquipmentAssignments(
                                    "d5079fe5-e81c-454d-a170-8530331d8833",
                                    "equipment_assignments",
                                    equip.id,
                                    null
                                )
                                assignments.addAll(equipmentAssignmentsResponse)
                            } catch (e: Exception) {
                                Log.e("EquipmentTracking", "Error loading assignments for equipment ${equip.id}: ${e.message}")
                            }
                        }
                        equipmentAssignments = assignments
                        
                        Toast.makeText(context, "Equipment checked out successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("EquipmentTracking", "Error checking out equipment: ${e.message}")
                        Toast.makeText(context, "Error checking out equipment: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                showCheckoutDialog = false
            }
        )
    }
    
    if (showCheckinDialog && selectedEquipment != null) {
        val currentAssignment = getCurrentAssignment(selectedEquipment!!.id, equipmentAssignments)
        val assignedWorker = if (currentAssignment != null) {
            workers.find { it.id == currentAssignment.workerId }
        } else null
        
        if (currentAssignment != null && assignedWorker != null) {
            CheckinEquipmentDialog(
                equipment = selectedEquipment!!,
                worker = assignedWorker,
                onDismiss = { showCheckinDialog = false },
                onCheckin = { newCondition ->
                    coroutineScope.launch {
                        try {
                            val now = Date()
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                            val timestamp = dateFormat.format(now)
                            
                            // Update equipment assignment
                            val updateAssignmentRequest = UpdateEquipmentAssignmentRequest(
                                appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                tableName = "equipment_assignments",
                                data = UpdateEquipmentAssignmentRequest.EquipmentAssignmentData(
                                    id = currentAssignment.id,
                                    equipmentId = selectedEquipment!!.id,
                                    workerId = assignedWorker.id,
                                    checkedOut = currentAssignment.checkedOut,
                                    checkedIn = timestamp
                                )
                            )
                            apiService.updateEquipmentAssignment(updateAssignmentRequest)
                            
                            // Update equipment status and condition
                            val updateEquipmentRequest = UpdateEquipmentRequest(
                                appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                tableName = "equipment",
                                data = UpdateEquipmentRequest.EquipmentData(
                                    id = selectedEquipment!!.id,
                                    name = selectedEquipment!!.name,
                                    type = selectedEquipment!!.type,
                                    status = "available",
                                    condition = newCondition,
                                    lastMaintenance = selectedEquipment!!.lastMaintenance
                                )
                            )
                            apiService.updateEquipment(updateEquipmentRequest)
                            
                            // Refresh data
                            val equipmentResponse = apiService.getEquipment("d5079fe5-e81c-454d-a170-8530331d8833", "equipment")
                            equipment = equipmentResponse
                            
                            val assignments = mutableListOf<EquipmentAssignment>()
                            for (equip in equipment) {
                                try {
                                    val equipmentAssignmentsResponse = apiService.getEquipmentAssignments(
                                        "d5079fe5-e81c-454d-a170-8530331d8833",
                                        "equipment_assignments",
                                        equip.id,
                                        null
                                    )
                                    assignments.addAll(equipmentAssignmentsResponse)
                                } catch (e: Exception) {
                                    Log.e("EquipmentTracking", "Error loading assignments for equipment ${equip.id}: ${e.message}")
                                }
                            }
                            equipmentAssignments = assignments
                            
                            Toast.makeText(context, "Equipment checked in successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("EquipmentTracking", "Error checking in equipment: ${e.message}")
                            Toast.makeText(context, "Error checking in equipment: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showCheckinDialog = false
                }
            )
        } else {
            showCheckinDialog = false
            Toast.makeText(context, "No active assignment found for this equipment", Toast.LENGTH_SHORT).show()
        }
    }
    
    if (showUpdateStatusDialog && selectedEquipment != null) {
        UpdateEquipmentStatusDialog(
            equipment = selectedEquipment!!,
            onDismiss = { showUpdateStatusDialog = false },
            onUpdateStatus = { newStatus ->
                coroutineScope.launch {
                    try {
                        val now = Date()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                        val timestamp = dateFormat.format(now)
                        
                        // Update equipment status
                        val updateEquipmentRequest = UpdateEquipmentRequest(
                            appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                            tableName = "equipment",
                            data = UpdateEquipmentRequest.EquipmentData(
                                id = selectedEquipment!!.id,
                                name = selectedEquipment!!.name,
                                type = selectedEquipment!!.type,
                                status = newStatus,
                                condition = selectedEquipment!!.condition,
                                lastMaintenance = if (newStatus == "maintenance") timestamp else selectedEquipment!!.lastMaintenance
                            )
                        )
                        apiService.updateEquipment(updateEquipmentRequest)
                        
                        // If equipment is being put into maintenance, check it in if it's checked out
                        if (newStatus == "maintenance") {
                            val currentAssignment = getCurrentAssignment(selectedEquipment!!.id, equipmentAssignments)
                            if (currentAssignment != null && currentAssignment.checkedIn == null) {
                                val updateAssignmentRequest = UpdateEquipmentAssignmentRequest(
                                    appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                    tableName = "equipment_assignments",
                                    data = UpdateEquipmentAssignmentRequest.EquipmentAssignmentData(
                                        id = currentAssignment.id,
                                        equipmentId = selectedEquipment!!.id,
                                        workerId = currentAssignment.workerId,
                                        checkedOut = currentAssignment.checkedOut,
                                        checkedIn = timestamp
                                    )
                                )
                                apiService.updateEquipmentAssignment(updateAssignmentRequest)
                            }
                        }
                        
                        // Refresh data
                        val equipmentResponse = apiService.getEquipment("d5079fe5-e81c-454d-a170-8530331d8833", "equipment")
                        equipment = equipmentResponse
                        
                        val assignments = mutableListOf<EquipmentAssignment>()
                        for (equip in equipment) {
                            try {
                                val equipmentAssignmentsResponse = apiService.getEquipmentAssignments(
                                    "d5079fe5-e81c-454d-a170-8530331d8833",
                                    "equipment_assignments",
                                    equip.id,
                                    null
                                )
                                assignments.addAll(equipmentAssignmentsResponse)
                            } catch (e: Exception) {
                                Log.e("EquipmentTracking", "Error loading assignments for equipment ${equip.id}: ${e.message}")
                            }
                        }
                        equipmentAssignments = assignments
                        
                        Toast.makeText(context, "Equipment status updated successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("EquipmentTracking", "Error updating equipment status: ${e.message}")
                        Toast.makeText(context, "Error updating equipment status: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                showUpdateStatusDialog = false
            }
        )
    }
}

fun getCurrentAssignment(equipmentId: String, assignments: List<EquipmentAssignment>): EquipmentAssignment? {
    return assignments.find { it.equipmentId == equipmentId && it.checkedIn == null }
}

@Composable
fun EquipmentItem(
    equipment: Equipment,
    assignedWorker: Worker?,
    onCheckout: () -> Unit,
    onCheckin: () -> Unit,
    onUpdateStatus: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val lastMaintenance = try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(equipment.lastMaintenance)
        date?.let {
            dateFormat.format(it)
        } ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
    
    val statusColor = when (equipment.status) {
        "available" -> Color(0xFF4CAF50) // Green
        "in_use" -> Color(0xFF2196F3) // Blue
        "maintenance" -> Color(0xFFFFA000) // Amber
        else -> Color.Gray
    }
    
    val conditionColor = when (equipment.condition) {
        "excellent" -> Color(0xFF4CAF50) // Green
        "good" -> Color(0xFF8BC34A) // Light Green
        "fair" -> Color(0xFFFFB74D) // Orange-ish
        "poor" -> Color(0xFFE57373) // Red-ish
        else -> Color.Gray
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equipment icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Equipment",
                        tint = MaterialTheme.colors.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = equipment.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    
                    Text(
                        text = equipment.type,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Chip(onClick = {}, backgroundColor = statusColor.copy(alpha = 0.2f)) {
                    Text(
                        text = equipment.status.replace("_", " ").capitalize(),
                        color = statusColor,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Condition",
                    modifier = Modifier.size(16.dp),
                    tint = conditionColor
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Condition: ${equipment.condition.capitalize()}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Last Maintenance",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colors.primary
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Maintenance: $lastMaintenance",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            
            if (assignedWorker != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Assigned To",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colors.primary
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Assigned to: ${assignedWorker.name} (${assignedWorker.role})",
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onUpdateStatus,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Update Status",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Status")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (equipment.status == "available") {
                    Button(
                        onClick = onCheckout,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Check Out",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check Out", color = Color.White)
                    }
                } else if (equipment.status == "in_use" && assignedWorker != null) {
                    Button(
                        onClick = onCheckin,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Check In",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check In", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEquipmentDialog(
    onDismiss: () -> Unit,
    onAddEquipment: (name: String, type: String, condition: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("good") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add New Equipment",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Equipment Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Equipment Type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )
                
                Text(
                    text = "Condition",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConditionChip(
                        text = "Excellent",
                        isSelected = condition == "excellent",
                        color = Color(0xFF4CAF50),
                        onClick = { condition = "excellent" }
                    )
                    
                    ConditionChip(
                        text = "Good",
                        isSelected = condition == "good",
                        color = Color(0xFF8BC34A),
                        onClick = { condition = "good" }
                    )
                    
                    ConditionChip(
                        text = "Fair",
                        isSelected = condition == "fair",
                        color = Color(0xFFFFB74D),
                        onClick = { condition = "fair" }
                    )
                    
                    ConditionChip(
                        text = "Poor",
                        isSelected = condition == "poor",
                        color = Color(0xFFE57373),
                        onClick = { condition = "poor" }
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank() && type.isNotBlank()) {
                                onAddEquipment(name, type, condition)
                            }
                        },
                        enabled = name.isNotBlank() && type.isNotBlank()
                    ) {
                        Text("Add Equipment")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutEquipmentDialog(
    equipment: Equipment,
    workers: List<Worker>,
    onDismiss: () -> Unit,
    onCheckout: (workerId: String) -> Unit
) {
    var selectedWorkerId by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Check Out Equipment",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = equipment.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (workers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active workers available for checkout")
                    }
                } else {
                    Text(
                        text = "Select worker to assign equipment to:",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(workers) { worker ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedWorkerId = worker.id }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedWorkerId == worker.id,
                                    onClick = { selectedWorkerId = worker.id }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = worker.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = worker.role,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onCheckout(selectedWorkerId) },
                        enabled = selectedWorkerId.isNotEmpty()
                    ) {
                        Text("Check Out")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckinEquipmentDialog(
    equipment: Equipment,
    worker: Worker,
    onDismiss: () -> Unit,
    onCheckin: (newCondition: String) -> Unit
) {
    var newCondition by remember { mutableStateOf(equipment.condition) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Check In Equipment",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = equipment.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Currently assigned to: ${worker.name}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Update equipment condition:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConditionChip(
                        text = "Excellent",
                        isSelected = newCondition == "excellent",
                        color = Color(0xFF4CAF50),
                        onClick = { newCondition = "excellent" }
                    )
                    
                    ConditionChip(
                        text = "Good",
                        isSelected = newCondition == "good",
                        color = Color(0xFF8BC34A),
                        onClick = { newCondition = "good" }
                    )
                    
                    ConditionChip(
                        text = "Fair",
                        isSelected = newCondition == "fair",
                        color = Color(0xFFFFB74D),
                        onClick = { newCondition = "fair" }
                    )
                    
                    ConditionChip(
                        text = "Poor",
                        isSelected = newCondition == "poor",
                        color = Color(0xFFE57373),
                        onClick = { newCondition = "poor" }
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(onClick = { onCheckin(newCondition) }) {
                        Text("Check In")
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateEquipmentStatusDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onUpdateStatus: (newStatus: String) -> Unit
) {
    var newStatus by remember { mutableStateOf(equipment.status) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Update Equipment Status",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = equipment.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Select new status:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { newStatus = "available" }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = newStatus == "available",
                            onClick = { newStatus = "available" }
                        )
                        Text(
                            text = "Available",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { newStatus = "maintenance" }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = newStatus == "maintenance",
                            onClick = { newStatus = "maintenance" }
                        )
                        Text(
                            text = "Maintenance",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    if (equipment.status == "in_use") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newStatus = "in_use" }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = newStatus == "in_use",
                                onClick = { newStatus = "in_use" }
                            )
                            Text(
                                text = "In Use (Keep current status)",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                if (newStatus == "maintenance") {
                    Text(
                        text = "Note: Putting equipment in maintenance will check it in if it's currently checked out.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onUpdateStatus(newStatus) },
                        enabled = newStatus != equipment.status
                    ) {
                        Text("Update Status")
                    }
                }
            }
        }
    }
}

@Composable
fun ConditionChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick),
        elevation = if (isSelected) 4.dp else 0.dp,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = text,
                color = if (isSelected) color else MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        elevation = if (selected) 4.dp else 0.dp,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.2f) else MaterialTheme.colors.surface
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun Chip(
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        elevation = 0.dp,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
