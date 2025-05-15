package com.example.tutorconnect.models

import java.util.Date

data class Comment(
    val id: String,
    val announcementId: String,
    val userId: String,
    val userName: String,
    val content: String,
    val createdAt: Date?,
    val isActive: Boolean = true
)
