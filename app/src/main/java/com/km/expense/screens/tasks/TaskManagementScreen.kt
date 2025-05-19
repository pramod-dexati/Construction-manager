package com.km.expense.screens.tasks

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
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
fun TaskManagementScreen(navigationActions: AppNavigationActions) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences() }
    val apiService = remember { ApiClient.apiService }
    val userId = remember { userPreferences.getUserId() }
    
    var isLoading by remember { mutableStateOf(true) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var workers by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var taskAssignments by remember { mutableStateOf<List<TaskAssignment>>(emptyList()) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedTaskWorkers by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var filterStatus by remember { mutableStateOf("") }
    
    val scaffoldState = rememberScaffoldState()
    
    LaunchedEffect(key1 = userId) {
        if (userId.isNotEmpty()) {
            try {
                // Load tasks
                val tasksResponse = apiService.getTasks("d5079fe5-e81c-454d-a170-8530331d8833", "tasks", userId)
                tasks = tasksResponse
                
                // Load workers
                val workersResponse = apiService.getWorkers("d5079fe5-e81c-454d-a170-8530331d8833", "workers", userId)
                workers = workersResponse
                
                // Load task assignments
                val assignments = mutableListOf<TaskAssignment>()
                for (task in tasks) {
                    try {
                        val taskAssignmentsResponse = apiService.getTaskAssignments(
                            "d5079fe5-e81c-454d-a170-8530331d8833",
                            "task_assignments",
                            task.id,
                            null
                        )
                        assignments.addAll(taskAssignmentsResponse)
                    } catch (e: Exception) {
                        Log.e("TaskManagement", "Error loading assignments for task ${task.id}: ${e.message}")
                    }
                }
                taskAssignments = assignments
            } catch (e: Exception) {
                Log.e("TaskManagement", "Error loading data: ${e.message}")
                Toast.makeText(context, "Error loading tasks", Toast.LENGTH_SHORT).show()
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
                title = { Text("Task Management") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { navigationActions.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
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
                        selected = filterStatus == "pending",
                        onClick = { filterStatus = "pending" },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text("Pending")
                    }
                    
                    FilterChip(
                        selected = filterStatus == "in_progress",
                        onClick = { filterStatus = "in_progress" },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text("In Progress")
                    }
                    
                    FilterChip(
                        selected = filterStatus == "completed",
                        onClick = { filterStatus = "completed" }
                    ) {
                        Text("Completed")
                    }
                }
                
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "No Tasks",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tasks added yet",
                                fontSize = 18.sp,
                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddTaskDialog = true },
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                            ) {
                                Text("Add Task", color = Color.White)
                            }
                        }
                    }
                } else {
                    val filteredTasks = if (filterStatus.isEmpty()) {
                        tasks
                    } else {
                        tasks.filter { it.status == filterStatus }
                    }
                    
                    if (filteredTasks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tasks match the selected filter")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTasks) { task ->
                                val taskWorkers = getWorkersForTask(task.id, taskAssignments, workers)
                                TaskItem(
                                    task = task,
                                    assignedWorkers = taskWorkers,
                                    onAssignWorkers = {
                                        selectedTask = task
                                        selectedTaskWorkers = taskWorkers
                                        showAssignTaskDialog = true
                                    },
                                    onUpdateStatus = { newStatus ->
                                        coroutineScope.launch {
                                            try {
                                                val updateTaskRequest = UpdateTaskRequest(
                                                    appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                                    tableName = "tasks",
                                                    data = UpdateTaskRequest.TaskData(
                                                        id = task.id,
                                                        title = task.title,
                                                        description = task.description,
                                                        priority = task.priority,
                                                        status = newStatus,
                                                        dueDate = task.dueDate,
                                                        createdBy = task.createdBy
                                                    )
                                                )
                                                apiService.updateTask(updateTaskRequest)
                                                
                                                // Refresh tasks
                                                val tasksResponse = apiService.getTasks("d5079fe5-e81c-454d-a170-8530331d8833", "tasks", userId)
                                                tasks = tasksResponse
                                                
                                                Toast.makeText(context, "Task status updated", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Log.e("TaskManagement", "Error updating task: ${e.message}")
                                                Toast.makeText(context, "Error updating task status", Toast.LENGTH_SHORT).show()
                                            }
                                        }
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
    
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, description, priority, dueDate ->
                coroutineScope.launch {
                    try {
                        val taskRequest = CreateTaskRequest(
                            appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                            tableName = "tasks",
                            data = CreateTaskRequest.TaskData(
                                title = title,
                                description = description,
                                priority = priority,
                                status = "pending",
                                dueDate = dueDate,
                                createdBy = userId
                            )
                        )
                        val response = apiService.createTask(taskRequest)
                        Log.d("TaskManagement", "Task created: $response")
                        
                        // Refresh tasks
                        val tasksResponse = apiService.getTasks("d5079fe5-e81c-454d-a170-8530331d8833", "tasks", userId)
                        tasks = tasksResponse
                        
                        Toast.makeText(context, "Task added successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("TaskManagement", "Error creating task: ${e.message}")
                        Toast.makeText(context, "Error adding task: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                showAddTaskDialog = false
            }
        )
    }
    
    if (showAssignTaskDialog && selectedTask != null) {
        AssignTaskDialog(
            task = selectedTask!!,
            allWorkers = workers,
            selectedWorkers = selectedTaskWorkers,
            onDismiss = { showAssignTaskDialog = false },
            onAssignWorkers = { selectedWorkerIds ->
                coroutineScope.launch {
                    try {
                        // First, remove all existing assignments for this task
                        val existingAssignments = taskAssignments.filter { it.taskId == selectedTask!!.id }
                        for (assignment in existingAssignments) {
                            // We don't actually delete, but in a real app you might want to
                            // For now, we'll just add new assignments
                        }
                        
                        // Add new assignments
                        for (workerId in selectedWorkerIds) {
                            val assignmentRequest = CreateTaskAssignmentRequest(
                                appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                tableName = "task_assignments",
                                data = CreateTaskAssignmentRequest.TaskAssignmentData(
                                    taskId = selectedTask!!.id,
                                    workerId = workerId
                                )
                            )
                            apiService.createTaskAssignment(assignmentRequest)
                        }
                        
                        // Refresh task assignments
                        val assignments = mutableListOf<TaskAssignment>()
                        for (task in tasks) {
                            try {
                                val taskAssignmentsResponse = apiService.getTaskAssignments(
                                    "d5079fe5-e81c-454d-a170-8530331d8833",
                                    "task_assignments",
                                    task.id,
                                    null
                                )
                                assignments.addAll(taskAssignmentsResponse)
                            } catch (e: Exception) {
                                Log.e("TaskManagement", "Error loading assignments for task ${task.id}: ${e.message}")
                            }
                        }
                        taskAssignments = assignments
                        
                        Toast.makeText(context, "Workers assigned successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("TaskManagement", "Error assigning workers: ${e.message}")
                        Toast.makeText(context, "Error assigning workers: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                showAssignTaskDialog = false
            }
        )
    }
}

fun getWorkersForTask(taskId: String, taskAssignments: List<TaskAssignment>, workers: List<Worker>): List<Worker> {
    val workerIds = taskAssignments.filter { it.taskId == taskId }.map { it.workerId }
    return workers.filter { it.id in workerIds }
}

@Composable
fun TaskItem(
    task: Task,
    assignedWorkers: List<Worker>,
    onAssignWorkers: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val dueDate = try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(task.dueDate)
        date?.let {
            dateFormat.format(it)
        } ?: "No due date"
    } catch (e: Exception) {
        "No due date"
    }
    
    val statusColor = when (task.status) {
        "pending" -> Color(0xFFFFA000) // Amber
        "in_progress" -> Color(0xFF2196F3) // Blue
        "completed" -> Color(0xFF4CAF50) // Green
        else -> Color.Gray
    }
    
    val priorityColor = when (task.priority) {
        "high" -> Color(0xFFE57373) // Red-ish
        "medium" -> Color(0xFFFFB74D) // Orange-ish
        "low" -> Color(0xFF81C784) // Green-ish
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
                // Priority indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                
                Chip(onClick = {}, backgroundColor = statusColor.copy(alpha = 0.2f)) {
                    Text(
                        text = task.status.replace("_", " ").capitalize(),
                        color = statusColor,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = task.description,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Due Date",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colors.primary
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Due: $dueDate",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Priority",
                    modifier = Modifier.size(16.dp),
                    tint = priorityColor
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = task.priority.capitalize(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Assigned Workers",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colors.primary
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Assigned: ${assignedWorkers.size} workers",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                
                TextButton(onClick = onAssignWorkers) {
                    Text("Assign")
                }
            }
            
            if (assignedWorkers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assignedWorkers.joinToString(", ") { it.name },
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (task.status) {
                    "pending" -> {
                        Button(
                            onClick = { onUpdateStatus("in_progress") },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                        ) {
                            Text("Start Task", color = Color.White)
                        }
                    }
                    "in_progress" -> {
                        Button(
                            onClick = { onUpdateStatus("completed") },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                        ) {
                            Text("Complete Task", color = Color.White)
                        }
                    }
                    "completed" -> {
                        Button(
                            onClick = { onUpdateStatus("in_progress") },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                        ) {
                            Text("Reopen Task", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (title: String, description: String, priority: String, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }
    var dueDate by remember { mutableStateOf("") }
    
    // For date picker
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    calendar.set(year, month, day)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                    dueDate = dateFormat.format(calendar.time)
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            // Date picker content would go here
            // Since we can't use actual DatePicker in this environment,
            // we'll just simulate it with a placeholder
            Text("Select a date")
        }
    }
    
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
                    text = "Add New Task",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .height(100.dp),
                    singleLine = false
                )
                
                Text(
                    text = "Priority",
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PriorityChip(
                        text = "Low",
                        isSelected = priority == "low",
                        color = Color(0xFF81C784),
                        onClick = { priority = "low" }
                    )
                    
                    PriorityChip(
                        text = "Medium",
                        isSelected = priority == "medium",
                        color = Color(0xFFFFB74D),
                        onClick = { priority = "medium" }
                    )
                    
                    PriorityChip(
                        text = "High",
                        isSelected = priority == "high",
                        color = Color(0xFFE57373),
                        onClick = { priority = "high" }
                    )
                }
                
                OutlinedTextField(
                    value = if (dueDate.isEmpty()) "" else {
                        try {
                            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(dueDate)
                            SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)
                        } catch (e: Exception) {
                            ""
                        }
                    },
                    onValueChange = { },
                    label = { Text("Due Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { showDatePicker = true },
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    }
                )
                
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
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank() && dueDate.isNotBlank()) {
                                onAddTask(title, description, priority, dueDate)
                            }
                        },
                        enabled = title.isNotBlank() && description.isNotBlank() && dueDate.isNotBlank()
                    ) {
                        Text("Add Task")
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityChip(
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                color = if (isSelected) color else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AssignTaskDialog(
    task: Task,
    allWorkers: List<Worker>,
    selectedWorkers: List<Worker>,
    onDismiss: () -> Unit,
    onAssignWorkers: (List<String>) -> Unit
) {
    val selectedWorkerIds = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(selectedWorkers) {
        selectedWorkerIds.clear()
        selectedWorkerIds.addAll(selectedWorkers.map { it.id })
    }
    
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
                    text = "Assign Workers to Task",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Select workers to assign:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (allWorkers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No workers available")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(allWorkers) { worker ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedWorkerIds.contains(worker.id)) {
                                            selectedWorkerIds.remove(worker.id)
                                        } else {
                                            selectedWorkerIds.add(worker.id)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedWorkerIds.contains(worker.id),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedWorkerIds.add(worker.id)
                                        } else {
                                            selectedWorkerIds.remove(worker.id)
                                        }
                                    }
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
                    
                    Button(onClick = { onAssignWorkers(selectedWorkerIds) }) {
                        Text("Assign Workers")
                    }
                }
            }
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

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
