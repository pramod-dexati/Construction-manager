package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class Equipment(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val condition: String,
    @SerializedName("last_maintenance") val lastMaintenance: String
)

data class CreateEquipmentRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: EquipmentData
) {
    data class EquipmentData(
        val name: String,
        val type: String,
        val status: String,
        val condition: String,
        @SerializedName("last_maintenance") val lastMaintenance: String
    )
}

data class UpdateEquipmentRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: EquipmentData
) {
    data class EquipmentData(
        val id: String,
        val name: String,
        val type: String,
        val status: String,
        val condition: String,
        @SerializedName("last_maintenance") val lastMaintenance: String
    )
}
