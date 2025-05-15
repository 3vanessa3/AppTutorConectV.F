package com.example.tutorconnect

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.adapters.AnnouncementAdapter
import com.example.tutorconnect.adapters.CommentAdapter
import com.example.tutorconnect.models.Announcement
import com.example.tutorconnect.models.AnnouncementType
import com.example.tutorconnect.models.Comment
import com.example.tutorconnect.fragments.CommentDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AnnouncementsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnnouncementAdapter
    private val announcements = mutableListOf<Announcement>()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcements)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAnnouncements)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = AnnouncementAdapter(
            announcements = announcements,
            onLikeClick = { announcement: Announcement, likeButton: ImageButton -> handleLike(announcement, likeButton) },
            onCommentClick = { announcement: Announcement -> showCommentsDialog(announcement) }
        )
        recyclerView.adapter = adapter

        // Configurar FAB para crear nuevo anuncio (solo visible para tutores)
        val fab: FloatingActionButton = findViewById(R.id.fabCreateAnnouncement)
        // TODO: Verificar si el usuario es tutor
        fab.setOnClickListener {
            showCreateAnnouncementDialog()
        }

        // Configurar botón de retroceso
        findViewById<View>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        // Cargar anuncios
        loadAnnouncements()
    }

    private fun handleLike(announcement: Announcement, likeButton: ImageButton) {
        announcement.isLikedByUser = !announcement.isLikedByUser
        if (announcement.isLikedByUser) {
            announcement.likesCount++
            likeButton.setColorFilter(ContextCompat.getColor(this, R.color.primary))
        } else {
            announcement.likesCount--
            likeButton.setColorFilter(ContextCompat.getColor(this, R.color.gray_dark))
        }
        adapter.notifyDataSetChanged()
        // TODO: Actualizar like en la base de datos
    }

    private fun showCommentsDialog(announcement: Announcement) {
        val dialog = CommentDialogFragment.newInstance(announcement.id)
        dialog.show(supportFragmentManager, "CommentDialog")
    }



    private fun showCreateAnnouncementDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_announcement)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val titleInput = dialog.findViewById<TextInputEditText>(R.id.titleInput)
        val descriptionInput = dialog.findViewById<TextInputEditText>(R.id.descriptionInput)
        val typeSpinner = dialog.findViewById<Spinner>(R.id.typeSpinner)
        val createButton = dialog.findViewById<Button>(R.id.createButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)

        // Configurar spinner
        val types = AnnouncementType.values()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter

        createButton.setOnClickListener {
            val title = titleInput.text.toString()
            val description = descriptionInput.text.toString()
            val type = types[typeSpinner.selectedItemPosition]

            if (title.isNotEmpty() && description.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                
                // Obtener el usuario actual
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Obtener el nombre del profesor
                db.collection("Profesores").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        val autorNombre = document.getString("Nombre") ?: "Nombre del Tutor"
                        
                        // Crear el mapa de datos
                        val comunicadoMap = hashMapOf(
                            "autorNombre" to autorNombre,
                            "titulo" to title,
                            "descripcion" to description,
                            "tipo" to type.toString(),
                            "fechaCreacion" to Date(),
                            "likesCount" to 0,
                            "commentsCount" to 0L
                        )

                        // Guardar en Firestore
                        db.collection("comunicados").document()
                            .set(comunicadoMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Comunicado creado exitosamente", Toast.LENGTH_SHORT).show()
                                loadAnnouncements() // Recargar la lista
                                dialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al crear comunicado: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al obtener información del usuario: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadAnnouncements() {
        val db = FirebaseFirestore.getInstance()
        db.collection("comunicados").get()
            .addOnSuccessListener { documents ->
                announcements.clear()
                for (document in documents) {
                    val docId = document.id
                    val data = document.data
                    
                    // Obtener el conteo real de comentarios
                    db.collection("comentarios")
                        .whereEqualTo("comunicadoId", docId)
                        .whereEqualTo("activo", true)
                        .get()
                        .addOnSuccessListener { commentDocs ->
                            val realCommentCount = commentDocs.size().toLong()
                            
                            // Actualizar el contador si es diferente
                            if (realCommentCount != (data["commentsCount"] as? Long ?: 0L)) {
                                db.collection("comunicados").document(docId)
                                    .update("commentsCount", realCommentCount)
                            }
                            
                            val announcement = Announcement(
                                id = docId,
                                tutorId = "",  // No necesario por ahora
                                tutorName = data["autorNombre"] as? String ?: "",
                                title = data["titulo"] as? String ?: "",
                                description = data["descripcion"] as? String ?: "",
                                type = when(data["tipo"] as? String ?: "") {
                                    "EVENT" -> AnnouncementType.EVENT
                                    "COURSE" -> AnnouncementType.COURSE
                                    "NEWS" -> AnnouncementType.NEWS
                                    "NOTICE" -> AnnouncementType.NOTICE
                                    else -> AnnouncementType.NOTICE
                                },
                                imageUrl = null,
                                createdAt = (data["fechaCreacion"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                                likesCount = (data["likesCount"] as? Long)?.toInt() ?: 0,
                                commentsCount = realCommentCount
                            )
                            announcements.add(announcement)
                            adapter.notifyDataSetChanged()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar comunicados: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
