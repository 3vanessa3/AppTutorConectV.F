package com.example.tutorconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.models.Professor

class ProfessorAdapter(
    private val onProfessorClick: (Professor) -> Unit
) : RecyclerView.Adapter<ProfessorAdapter.ViewHolder>() {
    private var allProfessors = mutableListOf<Professor>()
    private var filteredProfessors = mutableListOf<Professor>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.professorName)
        val subjectText: TextView = view.findViewById(R.id.professorSubject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_professor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val professor = filteredProfessors[position]
        holder.nameText.text = professor.nombre
        holder.subjectText.text = professor.especialidad
        holder.itemView.setOnClickListener { onProfessorClick(professor) }
    }

    override fun getItemCount() = filteredProfessors.size

    fun updateProfessors(newProfessors: List<Professor>) {
        allProfessors.clear()
        allProfessors.addAll(newProfessors)
        filteredProfessors.clear()
        filteredProfessors.addAll(newProfessors)
        notifyDataSetChanged()
    }

    fun filterProfessors(query: String) {
        filteredProfessors.clear()
        if (query.isEmpty()) {
            filteredProfessors.addAll(allProfessors)
        } else {
            filteredProfessors.addAll(allProfessors.filter { professor ->
                professor.nombre.contains(query, ignoreCase = true) ||
                professor.especialidad.contains(query, ignoreCase = true)
            })
        }
        notifyDataSetChanged()
    }
}
