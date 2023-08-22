package com.marko112.lecturepresenceapp

import android.media.Image
import java.util.Date

data class Lecture(
    var id: String?,
    val date: String?,
    val type: String,
    val number: Int?,
    val professorId: String?,
    val studentIds: MutableList<String>,
)