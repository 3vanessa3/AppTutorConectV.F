package com.example.tutorconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.fragments.Professor

class ProfessorsAdapter(
    private var professors: List<Professor>,
    private val onProfessorClick: (Professor) -> Unit
) : RecyclerView.Adapter<ProfessorsAdapter.ProfessorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_professor, parent, false)
        return ProfessorViewHolder(view, onProfessorClick)
    }

    override fun onBindViewHolder(holder: ProfessorViewHolder, position: Int) {
        holder.bind(professors[position])
    }

    override fun getItemCount() = professors.size

    fun updateProfessors(newProfessors: List<Professor>) {
        professors = newProfessors
        notifyDataSetChanged()
    }

    class ProfessorViewHolder(
        view: View,
        private val onProfessorClick: (Professor) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        private val txtEspecialidades: TextView = view.findViewById(R.id.txtEspecialidades)
        private val txtCalificacion: TextView = view.findViewById(R.id.txtCalificacion)
        private var currentProfessor: Professor? = null

        init {
            view.setOnClickListener {
                currentProfessor?.let { professor ->
                    onProfessorClick(professor)
                }
            }
        }

        fun bind(professor: Professor) {
            currentProfessor = professor
            txtNombre.text = professor.name
            txtEspecialidades.text = professor.subjects.joinToString(", ")
            txtCalificacion.text = String.format("%.1f", professor.rating)
        }
    }
}
