package com.example.tutorconnect.models

import java.util.Date

data class Announcement(
    val id: String,
    val tutorId: String,
    val tutorName: String,
    val title: String,
    val description: String,
    val type: AnnouncementType,
    val imageUrl: String?,
    val createdAt: Date = Date(),
    var likesCount: Int = 0,
    var commentsCount: Long = 0,
    val isActive: Boolean = true,
    var isLikedByUser: Boolean = false
)
