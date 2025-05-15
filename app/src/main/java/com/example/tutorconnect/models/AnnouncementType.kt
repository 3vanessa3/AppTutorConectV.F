package com.example.tutorconnect.models

enum class AnnouncementType {
    EVENT,
    COURSE,
    NEWS,
    NOTICE;
    
    override fun toString(): String {
        return name
    }
}
