package com.example.tutorconnect.models

data class Calificacion(
    val id: String = "",
    val nombreEstudiante: String = "",
    val calificacion: Float = 0f,
    val feedback: String = "",
    val fecha: String = "",
    val profesorId: String = "",
    val estudianteId: String = ""
)
