package com.example.tutorconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.models.Agenda

class AgendasAdapter(
    private val agendas: List<Agenda>,
    private val onAgendaClick: (Agenda) -> Unit
) : RecyclerView.Adapter<AgendasAdapter.AgendaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgendaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda, parent, false)
        return AgendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgendaViewHolder, position: Int) {
        val agenda = agendas[position]
        holder.bind(agenda)
        holder.itemView.setOnClickListener { onAgendaClick(agenda) }
    }

    override fun getItemCount() = agendas.size

    class AgendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtHorario: TextView = itemView.findViewById(R.id.txtHorario)
        private val txtMateria: TextView = itemView.findViewById(R.id.txtMateria)
        private val txtTutor: TextView = itemView.findViewById(R.id.txtTutor)
        private val txtEstudiante: TextView = itemView.findViewById(R.id.txtEstudiante)

        fun bind(agenda: Agenda) {
            txtHorario.text = "${agenda.horaInicio} - ${agenda.horaFin}"
            txtMateria.text = agenda.materia
            txtTutor.text = "Tutor: ${agenda.nombreTutor}"
            txtEstudiante.text = "Estudiante: ${agenda.nombreEstudiante}"
        }
    }
} 