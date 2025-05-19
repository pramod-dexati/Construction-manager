package com.km.expense.screens.dashboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.km.expense.data.api.ApiClient
import com.km.expense.data.api.models.*
import com.km.expense.data.preferences.UserPreferences
import com.km.expense.navigation.AppNavigationActions
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navigationActions: AppNavigationActions) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences() }
    val apiService = remember { ApiClient.apiService }
    val userId = remember { userPreferences.getUserId() }
    
    var isLoading by remember { mutableStateOf(true) }
    var activeWorkers by remember { mutableStateOf(0) }
    var totalTasks by remember { mutableStateOf(0) }
    var pendingTasks by remember { mutableStateOf(0) }
    var equipmentInUse by remember { mutableStateOf(0) }
    var totalEquipment by remember { mutableStateOf(0) }
    var progressPercentage by remember { mutableStateOf(0) }
    
    val scaffoldState = rememberScaffoldState()
    
    LaunchedEffect(key1 = userId) {
        if (userId.isNotEmpty()) {
            try {
                // Fetch workers data
                val workersResponse = apiService.getWorkers("d5079fe5-e81c-454d-a170-8530331d8833", "workers", userId)
                activeWorkers = workersResponse.count { it.isActive }
                
                // Fetch tasks data
                val tasksResponse = apiService.getTasks("d5079fe5-e81c-454d-a170-8530331d8833", "tasks", userId)
                totalTasks = tasksResponse.size
                pendingTasks = tasksResponse.count { it.status == "pending" || it.status == "in_progress" }
                
                // Fetch equipment data
                val equipmentResponse = apiService.getEquipment("d5079fe5-e81c-454d-a170-8530331d8833", "equipment")
                totalEquipment = equipmentResponse.size
                equipmentInUse = equipmentResponse.count { it.status == "in_use" }
                
                // Fetch progress reports
                val progressReportsResponse = apiService.getProgressReports("d5079fe5-e81c-454d-a170-8530331d8833", "progress_reports", userId)
                if (progressReportsResponse.isNotEmpty()) {
                    // Get the latest progress report
                    val latestReport = progressReportsResponse.maxByOrNull { it.date }
                    progressPercentage = latestReport?.percentageComplete?.toInt() ?: 0
                }
            } catch (e: Exception) {
                Log.e("DashboardScreen", "Error fetching data: ${e.message}")
                scaffoldState.snackbarHostState.showSnackbar("Error loading dashboard data")
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
                title = { Text("Dashboard") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            userPreferences.clearUserData()
                            navigationActions.navigateToAuth()
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        content = { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Site Overview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DashboardCard(
                            title = "Active Workers",
                            value = "$activeWorkers",
                            icon = Icons.Default.People,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .clickable { navigationActions.navigateToWorkerManagement() }
                        )
                        
                        DashboardCard(
                            title = "Tasks",
                            value = "$pendingTasks/$totalTasks",
                            icon = Icons.Default.Assignment,
                            color = MaterialTheme.colors.secondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .clickable { navigationActions.navigateToTaskManagement() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DashboardCard(
                            title = "Equipment",
                            value = "$equipmentInUse/$totalEquipment",
                            icon = Icons.Default.Build,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .clickable { navigationActions.navigateToEquipmentTracking() }
                        )
                        
                        DashboardCard(
                            title = "Progress",
                            value = "$progressPercentage%",
                            icon = Icons.Default.Timeline,
                            color = Color(0xFF2196F3),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .clickable { navigationActions.navigateToProgressReport() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Quick Actions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    QuickActionButton(
                        text = "Manage Workers",
                        icon = Icons.Default.People,
                        onClick = { navigationActions.navigateToWorkerManagement() }
                    )
                    
                    QuickActionButton(
                        text = "Assign Tasks",
                        icon = Icons.Default.Assignment,
                        onClick = { navigationActions.navigateToTaskManagement() }
                    )
                    
                    QuickActionButton(
                        text = "Track Equipment",
                        icon = Icons.Default.Build,
                        onClick = { navigationActions.navigateToEquipmentTracking() }
                    )
                    
                    QuickActionButton(
                        text = "Submit Progress Report",
                        icon = Icons.Default.Timeline,
                        onClick = { navigationActions.navigateToProgressReport() }
                    )
                }
            }
        }
    )
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            )
            
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
        elevation = ButtonDefaults.elevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                color = MaterialTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go",
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
