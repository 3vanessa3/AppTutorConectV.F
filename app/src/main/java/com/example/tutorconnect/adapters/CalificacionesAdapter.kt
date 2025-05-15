package com.example.tutorconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.models.Calificacion

class CalificacionesAdapter : ListAdapter<Calificacion, CalificacionesAdapter.CalificacionViewHolder>(
    object : DiffUtil.ItemCallback<Calificacion>() {
        override fun areItemsTheSame(oldItem: Calificacion, newItem: Calificacion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Calificacion, newItem: Calificacion): Boolean {
            return oldItem.nombreEstudiante == newItem.nombreEstudiante &&
                   oldItem.calificacion == newItem.calificacion &&
                   oldItem.feedback == newItem.feedback &&
                   oldItem.fecha == newItem.fecha
        }
    }
) {

    class CalificacionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtNombreEstudiante: TextView = view.findViewById(R.id.txtNombreEstudiante)
        private val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        private val txtCalificacion: TextView = view.findViewById(R.id.txtCalificacion)
        private val txtFecha: TextView = view.findViewById(R.id.txtFecha)
        private val cardFeedback: CardView = view.findViewById(R.id.cardFeedback)
        private val txtFeedback: TextView = view.findViewById(R.id.txtFeedback)

        fun bind(calificacion: Calificacion) {
            txtNombreEstudiante.text = calificacion.nombreEstudiante
            ratingBar.rating = calificacion.calificacion
            txtCalificacion.text = String.format("%.1f/5.0", calificacion.calificacion)
            txtFecha.text = calificacion.fecha
            
            if (calificacion.feedback.isNotEmpty()) {
                txtFeedback.text = calificacion.feedback
                // Inicialmente oculto
                cardFeedback.visibility = View.GONE
                
                // Click en cualquier parte del item muestra/oculta el feedback
                itemView.setOnClickListener {
                    cardFeedback.visibility = if (cardFeedback.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
            } else {
                cardFeedback.visibility = View.GONE
                itemView.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calificacion_recibida, parent, false)
        return CalificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalificacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
