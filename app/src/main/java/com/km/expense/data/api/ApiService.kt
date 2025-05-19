package com.km.expense.data.api

import com.km.expense.data.api.models.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("data")
    suspend fun registerUser(@Body request: RegisterRequest): User
    
    @POST("data/login")
    suspend fun loginUser(@Body request: LoginRequest): User
    
    // Workers
    @POST("data")
    suspend fun createWorker(@Body request: CreateWorkerRequest): Worker
    
    @POST("data")
    suspend fun updateWorker(@Body request: UpdateWorkerRequest): Worker
    
    @GET("data")
    suspend fun getWorkers(
        @Query("app_id") appId: String,
        @Query("table_name") tableName: String,
        @Query("user_id") userId: String
    ): List<Worker>
    
    // Attendance
    @POST("data")
    suspend fun createAttendance(@Body request: CreateAttendanceRequest): Attendance
    
    @POST("data")
    suspend fun updateAttendance(@Body request: UpdateAttendanceRequest): Attendance
    
    @GET("data")
    suspend fun getAttendance(
        @Query("app_id") appId: String,
        @Query("table_name") tableName: String,
        @Query("worker_id") workerId: String
    ): List<Attendance>
    
    // Tasks
    @POST("data")
    suspend fun createTask(@Body request: CreateTaskRequest): Task
    
    @POST("data")
    suspend fun updateTask(@Body request: UpdateTaskRequest): Task
    
    @GET("data")
    suspend fun getTasks(
        @Query("app_id") appId: String,
        @Query("table_name") tableName: String,
        @Query("created_by") createdBy: String
    ): List<Task>
    
    // Task Assignments
    @POST("data")
    suspend fun createTaskAssignment(@Body request: CreateTaskAssignmentRequest): TaskAssignment
    
    @POST("data")
    suspend fun updateTaskAssignment(@Body request: UpdateTaskAssignmentRequest): TaskAssignment
    
    @GET("data")
    suspend fun getTaskAssignments(
        @Query("app_id") appId: String,
        @Query("table_name") tableName: String,
        @Query("task_id") taskId: String,
        @Query("worker_id") workerId: String?
    ): List<TaskAssignment>
    
    // Equipment
    @POST("data")
    suspend fun createEquipment(@Body request: CreateEquipmentRequest): Equipment
    
    @POST("data")
    suspend fun updateEquipment(@Body request: UpdateEquipmentRequest): Equipment
    
    @GET("data")
    suspend fun getEquipment(
        @Query("app_id") appId: String,
        @Query("table_name") tableName: String
    ): List<Equipment>
    
    // Equipment Assignments
    @POST("data")
    suspend fun createEquipmentAssignment(@Body request: CreateEquipmentAssignmentRequest): EquipmentAssignment
    
    @POST("data")
    suspend fun updateEquipmentAssignment(@Body request: UpdateEquipmentAssignmentRequest): EquipmentAssignment
    
    @GET("data")
    suspend fun getEquipmentAssignments(
        @Query("app_id") appId: String,
        @Query("table_name") tableName: String,
        @Query("equipment_id") equipmentId: String,
        @Query("worker_id") workerId: String?
    ): List<EquipmentAssignment>
    
    // Progress Reports
    @POST("data")
    suspend fun createProgressReport(@Body request: CreateProgressReportRequest): ProgressReport
    
    @POST("data")
    suspend fun updateProgressReport(@Body request: UpdateProgressReportRequest): ProgressReport
    
    @GET("data")
    suspend fun getProgressReports(
        @Query("app_id") appId: String,
        @Query("table_name") tableName: String,
        @Query("submitted_by") submittedBy: String
    ): List<ProgressReport>
    
    // Report Photos
    @POST("data")
    suspend fun createReportPhoto(@Body request: CreateReportPhotoRequest): ReportPhoto
    
    @POST("data")
    suspend fun updateReportPhoto(@Body request: UpdateReportPhotoRequest): ReportPhoto
    
    @GET("data")
    suspend fun getReportPhotos(
        @Query("app_id") appId: String,
        @Query("table_name") tableName: String,
        @Query("report_id") reportId: String
    ): List<ReportPhoto>
    
    // File Upload
    @Multipart
    @POST("data/upload")
    suspend fun uploadFile(
        @Query("app_id") appId: String,
        @Query("user_id") userId: String,
        @Part file: MultipartBody.Part
    ): FileUploadResponse
}
