package com.km.expense.data.api.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val email: String?,
    val provider: String,
    @SerializedName("provider_username") val providerUsername: String?,
    @SerializedName("created_at") val createdAt: String
)

data class RegisterRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("table_name") val tableName: String,
    val data: UserData
) {
    data class UserData(
        val email: String,
        val password: String,
        val provider: String
    )
}

data class LoginRequest(
    @SerializedName("app_id") val appId: String,
    val email: String,
    val password: String,
    val provider: String
)

data class GoogleLoginRequest(
    @SerializedName("app_id") val appId: String,
    @SerializedName("provider_username") val providerUsername: String,
    val provider: String
)
