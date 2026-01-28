package com.app.sino.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CourseDataDto(
    @SerializedName("idCourse") val idCourse: Int,
    @SerializedName("dscCode") val dscCode: String,
    @SerializedName("dscName") val dscName: String
    // Add other fields from Course entity if needed in the frontend
)
