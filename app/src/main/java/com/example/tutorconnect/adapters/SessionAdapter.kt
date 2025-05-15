package com.example.tutorconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.models.SessionStatus
import com.example.tutorconnect.models.TutorSession

class SessionAdapter(
    private val onSessionClick: (TutorSession) -> Unit
) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {
    private var sessions = mutableListOf<TutorSession>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tutorName: TextView = view.findViewById(R.id.tutorName)
        val subject: TextView = view.findViewById(R.id.subject)
        val date: TextView = view.findViewById(R.id.date)
        val status: TextView = view.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.tutorName.text = session.tutorName
        holder.subject.text = session.subject
        holder.date.text = session.date
        holder.status.text = when (session.status) {
            SessionStatus.COMPLETED -> "Por calificar"
            SessionStatus.RATED -> "Calificado"
            SessionStatus.PENDING -> "Pendiente"
        }
        holder.itemView.setOnClickListener { onSessionClick(session) }
    }

    override fun getItemCount() = sessions.size

    fun updateSessions(newSessions: List<TutorSession>) {
        sessions.clear()
        sessions.addAll(newSessions)
        notifyDataSetChanged()
    }
}
