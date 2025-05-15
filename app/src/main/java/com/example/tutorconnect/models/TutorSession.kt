package com.example.tutorconnect.models

data class TutorSession(
    val id: String,
    val tutorName: String,
    val subject: String,
    val date: String,
    val status: SessionStatus = SessionStatus.PENDING,
    val rating: TutorRating? = null
)
