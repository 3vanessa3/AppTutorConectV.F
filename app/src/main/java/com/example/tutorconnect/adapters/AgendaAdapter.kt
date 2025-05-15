package com.example.tutorconnect.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.models.AgendaItem
import com.example.tutorconnect.AgendaDetalleActivity

class AgendaAdapter(private val agendaItems: List<AgendaItem>) : 
    RecyclerView.Adapter<AgendaAdapter.AgendaViewHolder>() {

    class AgendaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val background: LinearLayout = view.findViewById(R.id.itemBackground)
        val txtHorario: TextView = view.findViewById(R.id.txtHorario)
        val txtMateria: TextView = view.findViewById(R.id.txtMateria)
        val txtTutor: TextView = view.findViewById(R.id.txtTutor)
        val txtEstudiante: TextView = view.findViewById(R.id.txtEstudiante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgendaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda, parent, false)
        return AgendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgendaViewHolder, position: Int) {
        val item = agendaItems[position]
        
        // Configurar el fondo morado si es el último item (nuevo)
        if (position == agendaItems.size - 1) {
            holder.background.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.light_purple)
            )
        } else {
            holder.background.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        }

        holder.txtHorario.text = "${item.horaInicio} - ${item.horaFin}"
        holder.txtMateria.text = item.materia
        holder.txtTutor.text = "Tutor: ${item.tutor}"
        holder.txtEstudiante.text = "Estudiante: ${item.estudiante}"

        // Agregar clic al item
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AgendaDetalleActivity::class.java)
            intent.putExtra("AGENDA_ITEM", item)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = agendaItems.size
} 