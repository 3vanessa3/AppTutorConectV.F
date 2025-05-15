package com.example.tutorconnect.models

import java.io.Serializable

data class StudentTutoria(
    val materia: String,
    val hora: String,
    val tutor: String,
    val valor: String,
    val fecha: String,
    val enlaceMeet: String
) : Serializable {
    companion object {
        fun createExampleTutoria(): StudentTutoria {
            return StudentTutoria(
                materia = "Matemáticas",
                hora = "14:00 - 15:30",
                tutor = "Juan Pérez",
                valor = "\$70.000",
                fecha = "2025-05-05",
                enlaceMeet = "https://meet.google.com/example-meeting"
            )
        }
    }
}
