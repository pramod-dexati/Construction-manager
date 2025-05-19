package com.km.expense.screens.workers

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
import com.km.expense.data.api.ApiService
import com.km.expense.data.api.models.*
import com.km.expense.data.preferences.UserPreferences
import com.km.expense.navigation.AppNavigationActions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkerManagementScreen(navigationActions: AppNavigationActions) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences() }
    val apiService = remember { ApiClient.apiService }
    val userId = remember { userPreferences.getUserId() }
    
    var isLoading by remember { mutableStateOf(true) }
    var workers by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var showAttendanceDialog by remember { mutableStateOf(false) }
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var attendanceHistory by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var showAttendanceHistoryDialog by remember { mutableStateOf(false) }
    
    val scaffoldState = rememberScaffoldState()
    
    LaunchedEffect(key1 = userId) {
        if (userId.isNotEmpty()) {
            loadWorkers(apiService, userId) { result ->
                workers = result
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
                title = { Text("Worker Management") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { navigationActions.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddWorkerDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Worker")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWorkerDialog = true },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Worker", tint = Color.White)
            }
        },
        content = { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (workers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "No Workers",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No workers added yet",
                            fontSize = 18.sp,
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddWorkerDialog = true },
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                        ) {
                            Text("Add Worker", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workers) { worker ->
                        WorkerItem(
                            worker = worker,
                            onCheckInOut = {
                                selectedWorker = worker
                                showAttendanceDialog = true
                            },
                            onViewHistory = {
                                selectedWorker = worker
                                coroutineScope.launch {
                                    try {
                                        val history = apiService.getAttendance(
                                            "d5079fe5-e81c-454d-a170-8530331d8833",
                                            "attendance",
                                            worker.id
                                        )
                                        attendanceHistory = history
                                        showAttendanceHistoryDialog = true
                                    } catch (e: Exception) {
                                        Log.e("WorkerManagement", "Error fetching attendance: ${e.message}")
                                        Toast.makeText(context, "Error loading attendance history", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
    
    if (showAddWorkerDialog) {
        AddWorkerDialog(
            onDismiss = { showAddWorkerDialog = false },
            onAddWorker = { name, role, phone ->
                coroutineScope.launch {
                    try {
                        val workerRequest = CreateWorkerRequest(
                            appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                            tableName = "workers",
                            data = CreateWorkerRequest.WorkerData(
                                userId = userId,
                                name = name,
                                role = role,
                                phone = phone,
                                isActive = false
                            )
                        )
                        val response = apiService.createWorker(workerRequest)
                        Log.d("WorkerManagement", "Worker created: $response")
                        Toast.makeText(context, "Worker added successfully", Toast.LENGTH_SHORT).show()
                        
                        // Reload workers
                        loadWorkers(apiService, userId) { result ->
                            workers = result
                        }
                    } catch (e: Exception) {
                        Log.e("WorkerManagement", "Error creating worker: ${e.message}")
                        Toast.makeText(context, "Error adding worker: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                showAddWorkerDialog = false
            }
        )
    }
    
    if (showAttendanceDialog && selectedWorker != null) {
        AttendanceDialog(
            worker = selectedWorker!!,
            onDismiss = { showAttendanceDialog = false },
            onCheckInOut = { isCheckIn ->
                coroutineScope.launch {
                    try {
                        val now = Date()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                        val timestamp = dateFormat.format(now)
                        
                        if (isCheckIn) {
                            // Check in
                            val attendanceRequest = CreateAttendanceRequest(
                                appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                tableName = "attendance",
                                data = CreateAttendanceRequest.AttendanceData(
                                    workerId = selectedWorker!!.id,
                                    checkIn = timestamp,
                                    checkOut = null
                                )
                            )
                            val response = apiService.createAttendance(attendanceRequest)
                            Log.d("WorkerManagement", "Attendance created: $response")
                            
                            // Update worker status
                            val updateWorkerRequest = UpdateWorkerRequest(
                                appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                tableName = "workers",
                                data = UpdateWorkerRequest.WorkerData(
                                    id = selectedWorker!!.id,
                                    userId = selectedWorker!!.userId,
                                    name = selectedWorker!!.name,
                                    role = selectedWorker!!.role,
                                    phone = selectedWorker!!.phone,
                                    isActive = true
                                )
                            )
                            apiService.updateWorker(updateWorkerRequest)
                        } else {
                            // Find the latest attendance record without checkout
                            val attendanceHistory = apiService.getAttendance(
                                "d5079fe5-e81c-454d-a170-8530331d8833",
                                "attendance",
                                selectedWorker!!.id
                            )
                            
                            val latestAttendance = attendanceHistory.find { it.checkOut == null }
                            if (latestAttendance != null) {
                                // Update attendance with checkout time
                                val updateAttendanceRequest = UpdateAttendanceRequest(
                                    appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                    tableName = "attendance",
                                    data = UpdateAttendanceRequest.AttendanceData(
                                        id = latestAttendance.id,
                                        workerId = selectedWorker!!.id,
                                        checkIn = latestAttendance.checkIn,
                                        checkOut = timestamp
                                    )
                                )
                                val response = apiService.updateAttendance(updateAttendanceRequest)
                                Log.d("WorkerManagement", "Attendance updated: $response")
                                
                                // Update worker status
                                val updateWorkerRequest = UpdateWorkerRequest(
                                    appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                    tableName = "workers",
                                    data = UpdateWorkerRequest.WorkerData(
                                        id = selectedWorker!!.id,
                                        userId = selectedWorker!!.userId,
                                        name = selectedWorker!!.name,
                                        role = selectedWorker!!.role,
                                        phone = selectedWorker!!.phone,
                                        isActive = false
                                    )
                                )
                                apiService.updateWorker(updateWorkerRequest)
                            }
                        }
                        
                        Toast.makeText(
                            context,
                            if (isCheckIn) "Worker checked in successfully" else "Worker checked out successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Reload workers
                        loadWorkers(apiService, userId) { result ->
                            workers = result
                        }
                    } catch (e: Exception) {
                        Log.e("WorkerManagement", "Error updating attendance: ${e.message}")
                        Toast.makeText(context, "Error updating attendance: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                showAttendanceDialog = false
            }
        )
    }
    
    if (showAttendanceHistoryDialog && selectedWorker != null) {
        AttendanceHistoryDialog(
            worker = selectedWorker!!,
            attendanceHistory = attendanceHistory,
            onDismiss = { showAttendanceHistoryDialog = false }
        )
    }
}

private suspend fun loadWorkers(apiService: ApiService, userId: String, onResult: (List<Worker>) -> Unit) {
    try {
        val workers = apiService.getWorkers("d5079fe5-e81c-454d-a170-8530331d8833", "workers", userId)
        onResult(workers)
    } catch (e: Exception) {
        Log.e("WorkerManagement", "Error loading workers: ${e.message}")
        onResult(emptyList())
    }
}

@Composable
fun WorkerItem(
    worker: Worker,
    onCheckInOut: () -> Unit,
    onViewHistory: () -> Unit
) {
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
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (worker.isActive) Color(0xFF4CAF50) else Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.name.first().toString().uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = worker.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = worker.role,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = worker.phone,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (worker.isActive) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                )
                
                Text(
                    text = if (worker.isActive) "Active" else "Inactive",
                    color = if (worker.isActive) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onViewHistory,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("History")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onCheckInOut,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (worker.isActive) Color(0xFFE57373) else Color(0xFF81C784)
                    )
                ) {
                    Icon(
                        imageVector = if (worker.isActive) Icons.Default.ExitToApp else Icons.Default.Login,
                        contentDescription = if (worker.isActive) "Check Out" else "Check In",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (worker.isActive) "Check Out" else "Check In", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onAddWorker: (name: String, role: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
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
                    text = "Add New Worker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank() && role.isNotBlank() && phone.isNotBlank()) {
                                onAddWorker(name, role, phone)
                            }
                        },
                        enabled = name.isNotBlank() && role.isNotBlank() && phone.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onCheckInOut: (isCheckIn: Boolean) -> Unit
) {
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (worker.isActive) "Check Out Worker" else "Check In Worker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (worker.isActive) Color(0xFF4CAF50) else Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.name.first().toString().uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = worker.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Text(
                    text = worker.role,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = if (worker.isActive) {
                        "Worker is currently checked in. Do you want to check them out?"
                    } else {
                        "Worker is currently checked out. Do you want to check them in?"
                    },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    
                    Button(
                        onClick = { onCheckInOut(!worker.isActive) },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (worker.isActive) Color(0xFFE57373) else Color(0xFF81C784)
                        )
                    ) {
                        Text(
                            text = if (worker.isActive) "Check Out" else "Check In",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceHistoryDialog(
    worker: Worker,
    attendanceHistory: List<Attendance>,
    onDismiss: () -> Unit
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${worker.name}'s Attendance",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (attendanceHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No attendance records found")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(attendanceHistory) { attendance ->
                            AttendanceHistoryItem(attendance = attendance)
                        }
                    }
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun AttendanceHistoryItem(attendance: Attendance) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
    
    val checkInDate = try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(attendance.checkIn)
        date?.let {
            dateFormat.format(it)
        } ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
    
    val checkInTime = try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(attendance.checkIn)
        date?.let {
            timeFormat.format(it)
        } ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
    
    val checkOutTime = if (attendance.checkOut != null) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(attendance.checkOut)
            date?.let {
                timeFormat.format(it)
            } ?: "Not checked out"
        } catch (e: Exception) {
            "Not checked out"
        }
    } else {
        "Not checked out"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = MaterialTheme.colors.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = checkInDate,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Check In", color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = "Check In",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(checkInTime, fontWeight = FontWeight.Medium)
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Check Out", color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Check Out",
                            modifier = Modifier.size(16.dp),
                            tint = if (attendance.checkOut != null) Color(0xFFE57373) else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(checkOutTime, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
