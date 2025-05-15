package com.example.tutorconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.models.SessionStatus
import com.example.tutorconnect.models.TutorSession

class TutorSessionAdapter(
    private var sessions: List<TutorSession>,
    private val onSessionClick: (TutorSession) -> Unit
) : RecyclerView.Adapter<TutorSessionAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tutorName: TextView = view.findViewById(R.id.txtTutorName)
        val subject: TextView = view.findViewById(R.id.txtSubject)
        val date: TextView = view.findViewById(R.id.txtDate)
        val status: TextView = view.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tutor_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.tutorName.text = session.tutorName
        holder.subject.text = session.subject
        holder.date.text = session.date
        
        // Configurar estado y color
        when (session.status) {
            SessionStatus.PENDING -> {
                holder.status.text = "Pendiente"
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray_dark))
            }
            SessionStatus.COMPLETED -> {
                holder.status.text = "Por evaluar"
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
            }
            SessionStatus.RATED -> {
                holder.status.text = "Ver evaluación"
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.primary))
            }
        }

        // Configurar click según el estado
        holder.itemView.setOnClickListener {
            onSessionClick(session)
        }

        // Configurar estilo visual según el estado
        holder.itemView.alpha = when (session.status) {
            SessionStatus.PENDING -> 0.7f
            else -> 1.0f
        }
    }

    override fun getItemCount() = sessions.size

    fun updateSessions(newSessions: List<TutorSession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }
}
