package com.marko112.lecturepresenceapp

data class ConstructionExercise(
    override val activityType: String,
    override val activityDuration: Int) : IActivity