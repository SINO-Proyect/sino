package com.app.sino.data.remote.dto

data class StudentCourseDto(
    val idStudentCourse: Int? = null,
    val idStatus: Int,
    val statusName: String? = null,
    val idUser: Int,
    val idCourse: Int,
    val numTimesTaken: Int
)
