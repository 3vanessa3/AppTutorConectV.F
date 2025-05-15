package com.example.tutorconnect.models

import java.io.Serializable

data class AgendaItem(
    val id: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val materia: String = "",
    val tutor: String = "",
    val estudiante: String = "",
    val linkReunion: String = "",
    val valor: String = ""
) : Serializable 