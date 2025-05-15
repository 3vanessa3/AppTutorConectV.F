package com.example.tutorconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.models.StudentTutoria

class StudentAgendaDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_agenda_detail)

        // Obtener la tutoría desde el Intent
        val tutoria = intent.getSerializableExtra("Tutorias") as? StudentTutoria
        
        if (tutoria != null) {
            // Configurar los TextViews con la información de la tutoría
            findViewById<TextView>(R.id.txtFechaHora).text = "${tutoria.fecha} - ${tutoria.hora}"
            findViewById<TextView>(R.id.txtMateria).text = tutoria.materia
            findViewById<TextView>(R.id.txtMateriaDescripcion).text = "Clase de ${tutoria.materia}"
            findViewById<TextView>(R.id.txtTutor).text = tutoria.tutor
            findViewById<TextView>(R.id.txtTutorInfo).text = "Profesor de ${tutoria.materia}"
            findViewById<TextView>(R.id.txtValor).text = tutoria.valor
            findViewById<TextView>(R.id.txtEnlaceMeetUrl).text = tutoria.enlaceMeet

            // Configurar el clic en el enlace de Meet
            findViewById<TextView>(R.id.txtEnlaceMeetUrl).setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tutoria.enlaceMeet))
                startActivity(intent)
            }
        }

        // Configurar botón volver
        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            finish()
        }
    }
}
