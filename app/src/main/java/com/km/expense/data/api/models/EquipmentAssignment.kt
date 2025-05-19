package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class EquipmentAssignment(
    val id: String,
    @SerializedName("equipment_id") val equipmentId: String,
    @SerializedName("worker_id") val workerId: String,
    @SerializedName("checked_out") val checkedOut: String,
    @SerializedName("checked_in") val checkedIn: String?
)

data class CreateEquipmentAssignmentRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: EquipmentAssignmentData
) {
    data class EquipmentAssignmentData(
        @SerializedName("equipment_id") val equipmentId: String,
        @SerializedName("worker_id") val workerId: String,
        @SerializedName("checked_out") val checkedOut: String,
        @SerializedName("checked_in") val checkedIn: String?
    )
}

data class UpdateEquipmentAssignmentRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: EquipmentAssignmentData
) {
    data class EquipmentAssignmentData(
        val id: String,
        @SerializedName("equipment_id") val equipmentId: String,
        @SerializedName("worker_id") val workerId: String,
        @SerializedName("checked_out") val checkedOut: String,
        @SerializedName("checked_in") val checkedIn: String?
    )
}
