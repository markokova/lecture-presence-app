package com.marko112.lecturepresenceapp

data class AuditoryExercise(
    override val activityType: String,
    override val activityDuration: Int): IActivity
