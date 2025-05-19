package com.km.expense.screens.progress

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.km.expense.data.api.ApiClient
import com.km.expense.data.api.models.*
import com.km.expense.data.preferences.UserPreferences
import com.km.expense.navigation.AppNavigationActions
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressReportScreen(navigationActions: AppNavigationActions) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences() }
    val apiService = remember { ApiClient.apiService }
    val userId = remember { userPreferences.getUserId() }
    
    var isLoading by remember { mutableStateOf(true) }
    var progressReports by remember { mutableStateOf<List<ProgressReport>>(emptyList()) }
    var reportPhotos by remember { mutableStateOf<Map<String, List<ReportPhoto>>>(emptyMap()) }
    var showAddReportDialog by remember { mutableStateOf(false) }
    var showPhotoDetailDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<ReportPhoto?>(null) }
    
    val scaffoldState = rememberScaffoldState()
    
    LaunchedEffect(key1 = userId) {
        if (userId.isNotEmpty()) {
            try {
                // Load progress reports
                val reportsResponse = apiService.getProgressReports("d5079fe5-e81c-454d-a170-8530331d8833", "progress_reports", userId)
                progressReports = reportsResponse.sortedByDescending { it.date }
                
                // Load photos for each report
                val photosMap = mutableMapOf<String, List<ReportPhoto>>()
                for (report in progressReports) {
                    try {
                        val photosResponse = apiService.getReportPhotos("d5079fe5-e81c-454d-a170-8530331d8833", "report_photos", report.id)
                        photosMap[report.id] = photosResponse
                    } catch (e: Exception) {
                        Log.e("ProgressReport", "Error loading photos for report ${report.id}: ${e.message}")
                    }
                }
                reportPhotos = photosMap
            } catch (e: Exception) {
                Log.e("ProgressReport", "Error loading data: ${e.message}")
                Toast.makeText(context, "Error loading progress reports", Toast.LENGTH_SHORT).show()
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
                title = { Text("Progress Reports") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { navigationActions.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddReportDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Report")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddReportDialog = true },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Report", tint = Color.White)
            }
        },
        content = { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (progressReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "No Reports",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No progress reports added yet",
                            fontSize = 18.sp,
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddReportDialog = true },
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                        ) {
                            Text("Add Progress Report", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(progressReports) { report ->
                        val photos = reportPhotos[report.id] ?: emptyList()
                        ProgressReportItem(
                            report = report,
                            photos = photos,
                            onPhotoClick = { photo ->
                                selectedPhoto = photo
                                showPhotoDetailDialog = true
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // For FAB space
                    }
                }
            }
        }
    )
    
    if (showAddReportDialog) {
        AddProgressReportDialog(
            onDismiss = { showAddReportDialog = false },
            onAddReport = { description, percentageComplete, photoUris, captions ->
                coroutineScope.launch {
                    try {
                        val now = Date()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val date = dateFormat.format(now)
                        
                        // Create progress report
                        val reportRequest = CreateProgressReportRequest(
                            appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                            tableName = "progress_reports",
                            data = CreateProgressReportRequest.ProgressReportData(
                                date = date,
                                description = description,
                                percentageComplete = percentageComplete.toDouble(),
                                submittedBy = userId
                            )
                        )
                        val reportResponse = apiService.createProgressReport(reportRequest)
                        Log.d("ProgressReport", "Report created: $reportResponse")
                        
                        // Upload photos if any
                        if (photoUris.isNotEmpty()) {
                            for (i in photoUris.indices) {
                                val uri = photoUris[i]
                                val caption = if (i < captions.size) captions[i] else ""
                                
                                // Upload photo
                                val file = File(uri.path ?: "")
                                if (file.exists()) {
                                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                                    
                                    val uploadResponse = apiService.uploadFile(
                                        "d5079fe5-e81c-454d-a170-8530331d8833",
                                        userId,
                                        body
                                    )
                                    
                                    // Create report photo entry
                                    val photoRequest = CreateReportPhotoRequest(
                                        appId = "d5079fe5-e81c-454d-a170-8530331d8833",
                                        tableName = "report_photos",
                                        data = CreateReportPhotoRequest.ReportPhotoData(
                                            reportId = reportResponse.id,
                                            photoUrl = uploadResponse.url,
                                            caption = caption
                                        )
                                    )
                                    apiService.createReportPhoto(photoRequest)
                                }
                            }
                        }
                        
                        // Refresh data
                        val reportsResponse = apiService.getProgressReports("d5079fe5-e81c-454d-a170-8530331d8833", "progress_reports", userId)
                        progressReports = reportsResponse.sortedByDescending { it.date }
                        
                        val photosMap = mutableMapOf<String, List<ReportPhoto>>()
                        for (report in progressReports) {
                            try {
                                val photosResponse = apiService.getReportPhotos("d5079fe5-e81c-454d-a170-8530331d8833", "report_photos", report.id)
                                photosMap[report.id] = photosResponse
                            } catch (e: Exception) {
                                Log.e("ProgressReport", "Error loading photos for report ${report.id}: ${e.message}")
                            }
                        }
                        reportPhotos = photosMap
                        
                        Toast.makeText(context, "Progress report added successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("ProgressReport", "Error creating report: ${e.message}")
                        Toast.makeText(context, "Error adding progress report: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                showAddReportDialog = false
            }
        )
    }
    
    if (showPhotoDetailDialog && selectedPhoto != null) {
        PhotoDetailDialog(
            photo = selectedPhoto!!,
            onDismiss = { showPhotoDetailDialog = false }
        )
    }
}

@Composable
fun ProgressReportItem(
    report: ProgressReport,
    photos: List<ReportPhoto>,
    onPhotoClick: (ReportPhoto) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val reportDate = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(report.date)
        date?.let {
            dateFormat.format(it)
        } ?: "Unknown date"
    } catch (e: Exception) {
        "Unknown date"
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
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = MaterialTheme.colors.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = reportDate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${report.percentageComplete.toInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = report.description,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            if (photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Photos (${photos.size})",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos) { photo ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .clickable { onPhotoClick(photo) }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(photo.photoUrl),
                                contentDescription = photo.caption,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddProgressReportDialog(
    onDismiss: () -> Unit,
    onAddReport: (description: String, percentageComplete: String, photoUris: List<Uri>, captions: List<String>) -> Unit
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var percentageComplete by remember { mutableStateOf("0") }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var photoCaptions by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUris = photoUris + it
            photoCaptions = photoCaptions + ""
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
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
                    text = "Add Progress Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(bottom = 16.dp),
                            singleLine = false
                        )
                        
                        Text(
                            text = "Completion Percentage",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Slider(
                            value = percentageComplete.toFloatOrNull() ?: 0f,
                            onValueChange = { percentageComplete = it.toInt().toString() },
                            valueRange = 0f..100f,
                            steps = 100,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0%")
                            Text("${percentageComplete.toIntOrNull() ?: 0}%", fontWeight = FontWeight.Bold)
                            Text("100%")
                        }
                        
                        OutlinedTextField(
                            value = percentageComplete,
                            onValueChange = { value ->
                                val filtered = value.filter { it.isDigit() }
                                val intValue = filtered.toIntOrNull() ?: 0
                                percentageComplete = minOf(intValue, 100).toString()
                            },
                            label = { Text("Percentage") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true
                        )
                        
                        Text(
                            text = "Photos (${photoUris.size})",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Photo")
                        }
                    }
                    
                    items(photoUris.size) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(photoUris[index]),
                                contentDescription = "Selected Photo",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            OutlinedTextField(
                                value = photoCaptions.getOrElse(index) { "" },
                                onValueChange = { newCaption ->
                                    val newCaptions = photoCaptions.toMutableList()
                                    if (index < newCaptions.size) {
                                        newCaptions[index] = newCaption
                                    } else {
                                        newCaptions.add(newCaption)
                                    }
                                    photoCaptions = newCaptions
                                },
                                label = { Text("Caption") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            
                            IconButton(
                                onClick = {
                                    photoUris = photoUris.filterIndexed { i, _ -> i != index }
                                    photoCaptions = photoCaptions.filterIndexed { i, _ -> i != index }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Photo")
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
                        onClick = {
                            if (description.isNotBlank() && percentageComplete.isNotBlank()) {
                                onAddReport(description, percentageComplete, photoUris, photoCaptions)
                            }
                        },
                        enabled = description.isNotBlank() && percentageComplete.isNotBlank()
                    ) {
                        Text("Submit Report")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoDetailDialog(
    photo: ReportPhoto,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(photo.photoUrl),
                        contentDescription = photo.caption,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                if (photo.caption.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = photo.caption,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}
