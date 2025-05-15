package com.example.tutorconnect.models

data class Agenda(
    val id: String,
    val horaInicio: String,
    val horaFin: String,
    val materia: String,
    val nombreTutor: String,
    val nombreEstudiante: String,
    val linkReunion: String,
    val valor: Double
) 