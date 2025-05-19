package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class ReportPhoto(
    val id: String,
    @SerializedName("report_id") val reportId: String,
    @SerializedName("photo_url") val photoUrl: String,
    val caption: String
)

data class CreateReportPhotoRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: ReportPhotoData
) {
    data class ReportPhotoData(
        @SerializedName("report_id") val reportId: String,
        @SerializedName("photo_url") val photoUrl: String,
        val caption: String
    )
}

data class UpdateReportPhotoRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: ReportPhotoData
) {
    data class ReportPhotoData(
        val id: String,
        @SerializedName("report_id") val reportId: String,
        @SerializedName("photo_url") val photoUrl: String,
        val caption: String
    )
}

data class FileUploadResponse(
    val url: String
)
