package com.marko112.lecturepresenceapp

data class LectureExercise(
    override val activityType: String,
    override val activityDuration: Int
):IActivity
