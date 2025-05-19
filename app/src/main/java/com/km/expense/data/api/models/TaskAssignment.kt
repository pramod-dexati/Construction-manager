package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class TaskAssignment(
    val id: String,
    @SerializedName("task_id") val taskId: String,
    @SerializedName("worker_id") val workerId: String
)

data class CreateTaskAssignmentRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: TaskAssignmentData
) {
    data class TaskAssignmentData(
        @SerializedName("task_id") val taskId: String,
        @SerializedName("worker_id") val workerId: String
    )
}

data class UpdateTaskAssignmentRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: TaskAssignmentData
) {
    data class TaskAssignmentData(
        val id: String,
        @SerializedName("task_id") val taskId: String,
        @SerializedName("worker_id") val workerId: String
    )
}
