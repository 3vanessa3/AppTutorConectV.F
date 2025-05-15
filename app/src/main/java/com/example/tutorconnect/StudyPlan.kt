package com.example.tutorconnect

data class StudyPlan(
    val id: String = System.currentTimeMillis().toString(),
    val subject: String,
    val objective: String,
    val startDate: String,
    val endDate: String,
    val timePerWeek: Int,
    val preferredDays: String
)