package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("created_by") val createdBy: String
)

data class CreateTaskRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: TaskData
) {
    data class TaskData(
        val title: String,
        val description: String,
        val priority: String,
        val status: String,
        @SerializedName("due_date") val dueDate: String,
        @SerializedName("created_by") val createdBy: String
    )
}

data class UpdateTaskRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: TaskData
) {
    data class TaskData(
        val id: String,
        val title: String,
        val description: String,
        val priority: String,
        val status: String,
        @SerializedName("due_date") val dueDate: String,
        @SerializedName("created_by") val createdBy: String
    )
}
