package com.example.tutorconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.models.Comment
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private val comments: MutableList<Comment>,
    private val currentUserId: String,
    private val onEditComment: (Comment) -> Unit,
    private val onDeleteComment: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.commentUserName)
        val content: TextView = view.findViewById(R.id.commentContent)
        val dateText: TextView = view.findViewById(R.id.commentTimestamp)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        
        holder.userName.text = comment.userName
        holder.content.text = comment.content
        val date = comment.createdAt
        holder.dateText.text = if (date != null) formatDate(date) else "Hace un momento"

        // Mostrar/ocultar botones de edición y eliminación
        val isCommentOwner = comment.userId == currentUserId
        holder.editButton.visibility = if (isCommentOwner) View.VISIBLE else View.GONE
        holder.deleteButton.visibility = if (isCommentOwner) View.VISIBLE else View.GONE

        holder.editButton.setOnClickListener { onEditComment(comment) }
        holder.deleteButton.setOnClickListener { onDeleteComment(comment) }
    }

    override fun getItemCount() = comments.size

    private fun formatDate(date: Date): String {
        val now = Calendar.getInstance()
        val commentDate = Calendar.getInstance().apply { time = date }
        
        return when {
            now.get(Calendar.DATE) == commentDate.get(Calendar.DATE) -> {
                // Si es hoy, mostrar la hora
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            now.get(Calendar.DATE) - commentDate.get(Calendar.DATE) == 1 -> {
                // Si fue ayer
                "Ayer"
            }
            else -> {
                // Para otros días mostrar la fecha
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            }
        }
    }
}
