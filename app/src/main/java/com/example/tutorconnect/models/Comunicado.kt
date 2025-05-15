package com.example.tutorconnect.models

data class Comunicado(
    val id: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val comentarios: Int = 0
)
