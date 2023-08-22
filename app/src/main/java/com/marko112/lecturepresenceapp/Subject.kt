package com.marko112.lecturepresenceapp

data class Subject(
    val id: String,
    val name: String,
    val totalAENum: Long,
    val totalLectureNum: Long,
    val totalLENum: Long,
    val totalCENum: Long,
    val AEDuration: Long,
    val LectureDuration: Long,
    val LEDuration: Long,
    val CEDuration: Long,
    )