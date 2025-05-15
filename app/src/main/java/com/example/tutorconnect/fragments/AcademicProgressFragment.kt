package com.example.tutorconnect.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AcademicProgressFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_academic_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerTutorias)
        setupTutoriasRecyclerView()
    }

    private fun setupTutoriasRecyclerView() {
        // Ejemplo de datos de tutorías
        val tutorias = listOf(
            Tutoria(
                materia = "Álgebra Lineal",
                profesor = "Prof. Juan Pérez",
                estado = "En progreso",
                fecha = "05 de Mayo, 2025",
                hora = "10:00 AM - 11:30 AM"
            ),
            Tutoria(
                materia = "Cálculo Diferencial",
                profesor = "Prof. María García",
                estado = "Completada",
                fecha = "03 de Mayo, 2025",
                hora = "2:00 PM - 3:30 PM"
            ),
            Tutoria(
                materia = "Programación Java",
                profesor = "Prof. Carlos López",
                estado = "En progreso",
                fecha = "07 de Mayo, 2025",
                hora = "4:00 PM - 5:30 PM"
            )
        )

        recyclerView.adapter = TutoriasAdapter(tutorias) { tutoria ->
            mostrarDetalleTutoria(tutoria)
        }
    }

    private fun mostrarDetalleTutoria(tutoria: Tutoria) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_tutoria_detalle, null)

        // Configurar las vistas del diálogo
        dialogView.findViewById<TextView>(R.id.txtMateriaDetalle).text = tutoria.materia
        dialogView.findViewById<TextView>(R.id.txtProfesorDetalle).text = tutoria.profesor
        dialogView.findViewById<TextView>(R.id.txtFechaDetalle).text = "Fecha: ${tutoria.fecha}"
        dialogView.findViewById<TextView>(R.id.txtHoraDetalle).text = "Hora: ${tutoria.hora}"
        dialogView.findViewById<TextView>(R.id.txtEstadoDetalle).text = tutoria.estado

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }
}

data class Tutoria(
    val materia: String,
    val profesor: String,
    val estado: String,
    val fecha: String,
    val hora: String
)

class TutoriasAdapter(
    private val tutorias: List<Tutoria>,
    private val onTutoriaClick: (Tutoria) -> Unit
) : RecyclerView.Adapter<TutoriasAdapter.TutoriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tutoria, parent, false)
        return TutoriaViewHolder(view, onTutoriaClick)
    }

    override fun onBindViewHolder(holder: TutoriaViewHolder, position: Int) {
        val tutoria = tutorias[position]
        holder.bind(tutoria)
    }

    override fun getItemCount() = tutorias.size

    class TutoriaViewHolder(
        view: View,
        private val onTutoriaClick: (Tutoria) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val txtMateria: TextView = view.findViewById(R.id.txtMateria)
        private val txtProfesor: TextView = view.findViewById(R.id.txtProfesor)
        private val txtEstado: TextView = view.findViewById(R.id.txtEstado)
        private var currentTutoria: Tutoria? = null

        init {
            view.setOnClickListener {
                currentTutoria?.let { tutoria ->
                    onTutoriaClick(tutoria)
                }
            }
        }

        fun bind(tutoria: Tutoria) {
            currentTutoria = tutoria
            txtMateria.text = tutoria.materia
            txtProfesor.text = tutoria.profesor
            txtEstado.text = tutoria.estado
        }
    }
}
