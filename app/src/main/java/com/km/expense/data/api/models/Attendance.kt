package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class Attendance(
    val id: String,
    @SerializedName("worker_id") val workerId: String,
    @SerializedName("check_in") val checkIn: String,
    @SerializedName("check_out") val checkOut: String?
)

data class CreateAttendanceRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: AttendanceData
) {
    data class AttendanceData(
        @SerializedName("worker_id") val workerId: String,
        @SerializedName("check_in") val checkIn: String,
        @SerializedName("check_out") val checkOut: String?
    )
}

data class UpdateAttendanceRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: AttendanceData
) {
    data class AttendanceData(
        val id: String,
        @SerializedName("worker_id") val workerId: String,
        @SerializedName("check_in") val checkIn: String,
        @SerializedName("check_out") val checkOut: String?
    )
}
