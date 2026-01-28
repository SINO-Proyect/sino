package com.app.sino.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("idUser") val idUser: Int? = null,
    @SerializedName("firebaseUid") val firebaseUid: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("progress") val progress: Double? = null,
    @SerializedName("degreeName") val degreeName: String? = null,
    @SerializedName("followersCount") val followersCount: Long? = null,
    @SerializedName("followingCount") val followingCount: Long? = null,
    @SerializedName("email") val email: String,
    @SerializedName("dateRegister") val dateRegister: String? = null,
    @SerializedName("datePurchase") val datePurchase: String? = null,
    @SerializedName("type") val type: String = "freemium",
    @SerializedName("lastLogin") val lastLogin: String? = null
)
