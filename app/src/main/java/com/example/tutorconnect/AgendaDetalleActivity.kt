package com.example.tutorconnect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.models.AgendaItem

class AgendaDetalleActivity : AppCompatActivity() {
    private lateinit var agendaItem: AgendaItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda_detalle)

        // Obtener el objeto AgendaItem
        val item = intent.getSerializableExtra("AGENDA_ITEM") as? AgendaItem

        if (item != null) {
            agendaItem = item
            // Mostrar datos en la vista
            findViewById<TextView>(R.id.txtFecha).text = agendaItem.fecha
            findViewById<TextView>(R.id.txtHorario).text = "${agendaItem.horaInicio} - ${agendaItem.horaFin}"
            findViewById<TextView>(R.id.txtMateria).text = agendaItem.materia
            findViewById<TextView>(R.id.txtTutor).text = agendaItem.tutor
            findViewById<TextView>(R.id.txtEstudiante).text = agendaItem.estudiante
            findViewById<TextView>(R.id.txtLinkReunion).text = agendaItem.linkReunion
            findViewById<TextView>(R.id.txtValor).text = agendaItem.valor

            // Botón volver
            findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
                finish()
            }

            // Botón editar
            findViewById<ImageButton>(R.id.btnEditar).setOnClickListener {
                val intent = Intent(this, CreateAgendaActivity::class.java).apply {
                    putExtra("MODO", "EDITAR")
                    putExtra("AGENDA_ITEM", agendaItem)
                }
                startActivity(intent)
            }

            // Botón eliminar
            findViewById<ImageButton>(R.id.btnEliminar).setOnClickListener {
                mostrarDialogoConfirmacion()
            }
        }
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar tutoría")
            .setMessage("¿Estás seguro de que deseas eliminar esta tutoría?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarTutoria()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarTutoria() {
        // TODO: Implementar lógica para eliminar la tutoría de la base de datos
        // Por ahora solo cerramos la actividad
        val resultIntent = Intent()
        resultIntent.putExtra("TUTORIA_ELIMINADA", agendaItem.id)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
} 