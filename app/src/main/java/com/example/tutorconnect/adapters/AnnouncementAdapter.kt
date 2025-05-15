package com.example.tutorconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.models.Announcement
import com.example.tutorconnect.models.AnnouncementType
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementAdapter(
    private val announcements: List<Announcement>,
    private val onLikeClick: (Announcement, ImageButton) -> Unit,
    private val onCommentClick: (Announcement) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    class AnnouncementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.announcementTitle)
        val description: TextView = view.findViewById(R.id.announcementDescription)
        val tutorName: TextView = view.findViewById(R.id.tutorName)
        val date: TextView = view.findViewById(R.id.announcementDate)
        val typeLabel: TextView = view.findViewById(R.id.typeLabel)
        val announcementImage: ImageView = view.findViewById(R.id.announcementImage)
        val likeButton: ImageButton = view.findViewById(R.id.likeButton)
        val commentButton: ImageButton = view.findViewById(R.id.commentButton)
        val likesCount: TextView = view.findViewById(R.id.likesCount)
        val commentsCount: TextView = view.findViewById(R.id.commentsCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.title.text = announcement.title
        holder.description.text = announcement.description
        holder.tutorName.text = announcement.tutorName

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.date.text = dateFormat.format(announcement.createdAt)
        holder.likesCount.text = announcement.likesCount.toString()
        // Solo mostrar el contador que viene del anuncio
        holder.commentsCount.text = announcement.commentsCount.toString()

        // Configurar el color del corazón
        if (announcement.isLikedByUser) {
            holder.likeButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.purple_500))
        } else {
            holder.likeButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.gray_dark))
        }

        // Configurar la etiqueta de tipo con texto blanco
        holder.typeLabel.apply {
            text = announcement.type.name
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            background = when (announcement.type) {
                AnnouncementType.EVENT -> ContextCompat.getDrawable(context, R.drawable.bg_event_label)
                AnnouncementType.COURSE -> ContextCompat.getDrawable(context, R.drawable.bg_course_label)
                AnnouncementType.NEWS -> ContextCompat.getDrawable(context, R.drawable.bg_news_label)
                AnnouncementType.NOTICE -> ContextCompat.getDrawable(context, R.drawable.bg_news_label)
            }
        }

        // Configurar clics y estados
        holder.likeButton.setOnClickListener { 
            onLikeClick(announcement, holder.likeButton)
        }
        holder.commentButton.setOnClickListener { onCommentClick(announcement) }

        // Mostrar/ocultar imagen si existe
        if (announcement.imageUrl != null) {
            holder.announcementImage.visibility = View.VISIBLE
            // TODO: Cargar imagen usando Glide o Picasso
        } else {
            holder.announcementImage.visibility = View.GONE
        }
    }

    override fun getItemCount() = announcements.size
}
