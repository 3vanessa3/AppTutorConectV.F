package com.example.tutorconnect.models

data class TutorRating(
    val sessionId: String,
    val tutorName: String,
    val subject: String,
    val date: String,
    val rating: Int,
    val feedback: String
)
