package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class ProgressReport(
    val id: String,
    val date: String,
    val description: String,
    @SerializedName("percentage_complete") val percentageComplete: Double,
    @SerializedName("submitted_by") val submittedBy: String
)

data class CreateProgressReportRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: ProgressReportData
) {
    data class ProgressReportData(
        val date: String,
        val description: String,
        @SerializedName("percentage_complete") val percentageComplete: Double,
        @SerializedName("submitted_by") val submittedBy: String
    )
}

data class UpdateProgressReportRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: ProgressReportData
) {
    data class ProgressReportData(
        val id: String,
        val date: String,
        val description: String,
        @SerializedName("percentage_complete") val percentageComplete: Double,
        @SerializedName("submitted_by") val submittedBy: String
    )
}
