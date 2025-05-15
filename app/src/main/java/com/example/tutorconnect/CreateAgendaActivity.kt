package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.models.AgendaItem
import java.text.SimpleDateFormat
import java.util.*

class CreateAgendaActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var etHoraInicio: EditText
    private lateinit var etHoraFin: EditText
    private lateinit var etMateria: EditText
    private lateinit var etTutor: EditText
    private lateinit var etEstudiante: EditText
    private lateinit var etLinkReunion: EditText
    private lateinit var etValor: EditText
    private lateinit var btnGuardar: Button
    private var selectedDate: String = ""
    private var isEditMode = false
    private var agendaId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Inicializar vistas
        initializeViews()

        // Verificar si estamos en modo edición
        isEditMode = intent.getStringExtra("MODO") == "EDITAR"
        if (isEditMode) {
            loadAgendaData()
        }

        // Configurar formato de fecha
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("es"))
        selectedDate = dateFormat.format(Date())

        // Actualizar fecha cuando se seleccione en el calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)
        }

        // Botón volver
        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            finish()
        }

        // Botón guardar
        btnGuardar.setOnClickListener {
            if (validateFields()) {
                saveAgenda()
            }
        }
    }

    private fun initializeViews() {
        calendarView = findViewById(R.id.calendarView)
        etHoraInicio = findViewById(R.id.etHoraInicio)
        etHoraFin = findViewById(R.id.etHoraFin)
        etMateria = findViewById(R.id.etMateria)
        etTutor = findViewById(R.id.etTutor)
        etEstudiante = findViewById(R.id.etEstudiante)
        etLinkReunion = findViewById(R.id.etLinkReunion)
        etValor = findViewById(R.id.etValor)
        btnGuardar = findViewById(R.id.btnGuardar)
    }

    private fun loadAgendaData() {
        // Cargar datos de la agenda para edición
        agendaId = intent.getStringExtra("AGENDA_ID") ?: ""
        etHoraInicio.setText(intent.getStringExtra("horaInicio"))
        etHoraFin.setText(intent.getStringExtra("horaFin"))
        etMateria.setText(intent.getStringExtra("materia"))
        etTutor.setText(intent.getStringExtra("tutor"))
        etEstudiante.setText(intent.getStringExtra("estudiante"))
        etLinkReunion.setText(intent.getStringExtra("linkReunion"))
        etValor.setText(intent.getStringExtra("valor"))
        
        // Actualizar título y texto del botón
        findViewById<TextView>(R.id.txtTitulo)?.text = "Editar Tutoría"
        btnGuardar.text = "Guardar Cambios"
    }

    private fun validateFields(): Boolean {
        if (etHoraInicio.text.isNullOrEmpty() || 
            etHoraFin.text.isNullOrEmpty() ||
            etMateria.text.isNullOrEmpty() ||
            etTutor.text.isNullOrEmpty() ||
            etEstudiante.text.isNullOrEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveAgenda() {
        val agendaItem = AgendaItem(
            id = if (isEditMode) agendaId else UUID.randomUUID().toString(),
            fecha = selectedDate,
            horaInicio = etHoraInicio.text.toString(),
            horaFin = etHoraFin.text.toString(),
            materia = etMateria.text.toString(),
            tutor = etTutor.text.toString(),
            estudiante = etEstudiante.text.toString(),
            linkReunion = etLinkReunion.text.toString(),
            valor = etValor.text.toString()
        )

        // Devolver el resultado a la actividad anterior
        val resultIntent = Intent()
        resultIntent.putExtra("NEW_AGENDA_ITEM", agendaItem)
        setResult(RESULT_OK, resultIntent)

        val message = if (isEditMode) "Tutoría actualizada exitosamente" else "Tutoría agendada exitosamente"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
} 