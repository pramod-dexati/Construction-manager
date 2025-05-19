package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class Worker(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val name: String,
    val role: String,
    val phone: String,
    @SerializedName("is_active") val isActive: Boolean
)

data class CreateWorkerRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: WorkerData
) {
    data class WorkerData(
        @SerializedName("user_id") val userId: String,
        val name: String,
        val role: String,
        val phone: String,
        @SerializedName("is_active") val isActive: Boolean
    )
}

data class UpdateWorkerRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: WorkerData
) {
    data class WorkerData(
        val id: String,
        @SerializedName("user_id") val userId: String,
        val name: String,
        val role: String,
        val phone: String,
        @SerializedName("is_active") val isActive: Boolean
    )
}
