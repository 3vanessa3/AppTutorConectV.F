package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.models.StudentTutoria
import com.example.tutorconnect.StudentTutoriaAdapter
import java.util.*

class StudentCalendarActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentTutoriaAdapter
    private val tutorias = mutableListOf<StudentTutoria>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_calendar)

        // Configurar calendario
        val calendarView = findViewById<android.widget.CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Evitar redirecciones no deseadas
            if (isFinishing || isDestroyed) {
                return@setOnDateChangeListener
            }
            
            val date = Calendar.getInstance()
            date.set(year, month, dayOfMonth)
            mostrarTutoriasParaFecha(date)
        }

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewTutorias)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentTutoriaAdapter(tutorias, this)
        recyclerView.adapter = adapter

        // Configurar botón volver
        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            finish()
        }

        // Mostrar tutorías para la fecha actual
        mostrarTutoriasParaFecha(Calendar.getInstance())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun mostrarTutoriasParaFecha(date: Calendar) {
        // Aquí iría la lógica para cargar las tutorías del estudiante para la fecha seleccionada
        // Por ahora, mostramos tutorías de ejemplo
        tutorias.clear()
        tutorias.addAll(getTutoriasEjemplo(date))
        adapter.notifyDataSetChanged()
    }

    private fun getTutoriasEjemplo(date: Calendar): List<StudentTutoria> {
        // Ejemplo de tutorías
        return listOf(
            StudentTutoria.createExampleTutoria(),
            StudentTutoria.createExampleTutoria().copy(
                materia = "Física",
                hora = "16:00 - 17:30",
                tutor = "María García",
                valor = "\$80.000"
            )
        )
    }
}
