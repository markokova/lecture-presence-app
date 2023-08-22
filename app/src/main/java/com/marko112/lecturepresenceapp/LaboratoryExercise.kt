package com.marko112.lecturepresenceapp

data class LaboratoryExercise(
    override val activityType: String,
    override val activityDuration: Int) : IActivity