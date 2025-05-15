package com.example.tutorconnect

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.models.StudentTutoria

class StudentTutoriaAdapter(
    private val tutorias: List<StudentTutoria>,
    private val context: Context
) : RecyclerView.Adapter<StudentTutoriaAdapter.TutoriaViewHolder>() {

    class TutoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        val txtHora: TextView = itemView.findViewById(R.id.txtHora)
        val txtMateria: TextView = itemView.findViewById(R.id.txtMateria)
        val txtValor: TextView = itemView.findViewById(R.id.txtValor)
        val txtTutor: TextView = itemView.findViewById(R.id.txtTutor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_tutoria, parent, false)
        return TutoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutoriaViewHolder, position: Int) {
        val tutoria = tutorias[position]
        holder.txtFecha.text = tutoria.fecha
        holder.txtHora.text = tutoria.hora
        holder.txtMateria.text = tutoria.materia
        holder.txtValor.text = tutoria.valor
        holder.txtTutor.text = tutoria.tutor

        // Configurar clic en el item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, StudentAgendaDetailActivity::class.java)
            intent.putExtra("tutoria", tutoria)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = tutorias.size
}
