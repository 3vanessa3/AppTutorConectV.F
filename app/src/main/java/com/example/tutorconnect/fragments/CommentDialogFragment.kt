package com.example.tutorconnect.fragments

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.adapters.CommentAdapter
import com.example.tutorconnect.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class CommentDialogFragment : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var adapter: CommentAdapter
    private val comments = mutableListOf<Comment>()
    private var announcementId: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        fun newInstance(announcementId: String): CommentDialogFragment {
            val fragment = CommentDialogFragment()
            val args = Bundle()
            args.putString("announcementId", announcementId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        announcementId = arguments?.getString("announcementId")
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.white)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CommentDialog", "onViewCreated iniciado")
        recyclerView = view.findViewById(R.id.commentsRecyclerView)
        commentInput = view.findViewById(R.id.commentInput)
        sendButton = view.findViewById(R.id.sendCommentButton)

        setupRecyclerView()
        setupSendButton()
        
        // Verificar el ID del anuncio
        val anuncioId = announcementId
        Log.d("CommentDialog", "ID del anuncio: $anuncioId")
        
        if (anuncioId == null) {
            Toast.makeText(context, "Error: ID del anuncio no encontrado", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }
        
        loadComments()
    }

    private fun setupRecyclerView() {
        // Configurar el LayoutManager
        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        // Configurar el adaptador
        adapter = CommentAdapter(
            comments = comments,
            currentUserId = auth.currentUser?.uid ?: "",
            onEditComment = { comment -> handleEditComment(comment) },
            onDeleteComment = { comment -> handleDeleteComment(comment) }
        )
        recyclerView.adapter = adapter

        // Agregar decoración para espaciado entre items
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = 8
                outRect.bottom = 8
            }
        })
    }

    private fun setupSendButton() {
        Log.d("CommentDialog", "Configurando botón de enviar")
        sendButton.setOnClickListener {
            val content = commentInput.text.toString().trim()
            Log.d("CommentDialog", "Contenido del comentario: $content")
            
            if (content.isEmpty()) {
                Toast.makeText(context, "El comentario no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (announcementId == null) {
                Log.e("CommentDialog", "Error: announcementId es null")
                Toast.makeText(context, "Error: ID del anuncio no encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("CommentDialog", "Error: usuario no autenticado")
                Toast.makeText(context, "Debes iniciar sesión para comentar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Buscar el nombre del usuario en Profesores
            db.collection("Profesores").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("Nombre_Completo") ?: "Usuario"
                        Log.d("CommentDialog", "Profesor encontrado: $userName")
                        saveComment(content, userName)
                        commentInput.text.clear()
                    } else {
                        // Si no es profesor, buscar en estudiantes
                        db.collection("Estudiantes").document(currentUser.uid).get()
                            .addOnSuccessListener { studentDoc ->
                                val userName = studentDoc.getString("Nombre_Completo") ?: "Usuario"
                                Log.d("CommentDialog", "Estudiante encontrado: $userName")
                                saveComment(content, userName)
                                commentInput.text.clear()
                            }
                            .addOnFailureListener { e ->
                                Log.e("CommentDialog", "Error al obtener estudiante: ${e.message}")
                                Toast.makeText(context, "Error al obtener información del usuario", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CommentDialog", "Error al obtener profesor: ${e.message}")
                    Toast.makeText(context, "Error al obtener información del usuario", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveComment(content: String, userName: String) {
        Log.d("CommentDialog", "Guardando comentario: $content de usuario: $userName")
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("CommentDialog", "Error: usuario no autenticado al guardar comentario")
            Toast.makeText(context, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (announcementId == null) {
            Log.e("CommentDialog", "Error: announcementId es null al guardar comentario")
            Toast.makeText(context, "Error: ID del anuncio no encontrado", Toast.LENGTH_SHORT).show()
            return
        }
        
        val comment = hashMapOf(
            "comunicadoId" to announcementId,
            "usuarioId" to currentUser.uid,
            "nombreUsuario" to userName,
            "contenido" to content,
            "fechaCreacion" to Date(),
            "activo" to true
        )
        
        Log.d("CommentDialog", "Datos del comentario: $comment")
        
        db.collection("comentarios").add(comment)
            .addOnSuccessListener { documentReference ->
                Log.d("CommentDialog", "Comentario guardado con ID: ${documentReference.id}")
                
                val newComment = Comment(
                    id = documentReference.id,
                    announcementId = announcementId!!,
                    userId = currentUser.uid,
                    userName = userName,
                    content = content,
                    createdAt = Date(),
                    isActive = true
                )

                comments.add(0, newComment)  // Agregar al inicio de la lista
                adapter.notifyItemInserted(0)
                recyclerView.scrollToPosition(0)

                // Actualizar contador de comentarios en el anuncio
                db.collection("comunicados").document(announcementId!!)
                    .get()
                    .addOnSuccessListener { document ->
                        val currentCount = document.getLong("commentsCount") ?: 0
                        Log.d("CommentDialog", "Contador actual de comentarios: $currentCount")
                        
                        db.collection("comunicados").document(announcementId!!)
                            .update("commentsCount", currentCount + 1)
                            .addOnSuccessListener {
                                Log.d("CommentDialog", "Contador de comentarios actualizado a: ${currentCount + 1}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("CommentDialog", "Error al actualizar contador: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CommentDialog", "Error al obtener contador actual: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CommentDialog", "Error al guardar comentario: ${e.message}")
                Toast.makeText(context, "Error al guardar el comentario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadComments() {
        val anuncioId = announcementId
        Log.d("CommentDialog", "loadComments iniciado para anuncio: $anuncioId")
        
        if (anuncioId != null) {
            try {
                // Primero verificar que la colección existe
                db.collection("comentarios")
                    .limit(1)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        Log.d("CommentDialog", "Verificación de colección exitosa")
                        
                        // Ahora cargar los comentarios
                        db.collection("comentarios")
                            .whereEqualTo("comunicadoId", anuncioId)
                            .whereEqualTo("activo", true)
                            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener { documents ->
                                Log.d("CommentDialog", "Query exitoso. Comentarios encontrados: ${documents.size()}")
                                comments.clear()
                                
                                if (documents.isEmpty) {
                                    Log.d("CommentDialog", "No hay comentarios para este anuncio")
                                    return@addOnSuccessListener
                                }
                                
                                for (document in documents) {
                                    try {
                                        Log.d("CommentDialog", "Documento: ${document.id} - Datos: ${document.data}")
                                        
                                        val comment = Comment(
                                            id = document.id,
                                            announcementId = document.getString("comunicadoId") ?: "",
                                            userId = document.getString("usuarioId") ?: "",
                                            userName = document.getString("nombreUsuario") ?: "",
                                            content = document.getString("contenido") ?: "",
                                            createdAt = document.getDate("fechaCreacion") ?: Date(),
                                            isActive = document.getBoolean("activo") ?: true
                                        )
                                        comments.add(comment)
                                        Log.d("CommentDialog", "Comentario procesado: $comment")
                                    } catch (e: Exception) {
                                        Log.e("CommentDialog", "Error procesando documento ${document.id}: ${e.message}")
                                    }
                                }
                                
                                Log.d("CommentDialog", "Total de comentarios cargados: ${comments.size}")
                                adapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Log.e("CommentDialog", "Error en query de comentarios: ${e.message}")
                                Toast.makeText(context, "Error al cargar los comentarios", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CommentDialog", "Error verificando colección: ${e.message}")
                        Toast.makeText(context, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Log.e("CommentDialog", "Error inesperado en loadComments: ${e.message}")
                Toast.makeText(context, "Error inesperado al cargar comentarios", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("CommentDialog", "No se pueden cargar comentarios: announcementId es null")
            Toast.makeText(context, "Error: ID del anuncio no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleEditComment(comment: Comment) {
        // Poner el contenido del comentario en el input
        commentInput.setText(comment.content)
        commentInput.setSelection(comment.content.length)
        
        // Cambiar el botón de enviar por uno de actualizar
        sendButton.setOnClickListener {
            val newContent = commentInput.text.toString().trim()
            if (newContent.isNotEmpty()) {
                db.collection("comentarios").document(comment.id)
                    .update("contenido", newContent)
                    .addOnSuccessListener {
                        // Actualizar el comentario en la lista local
                        val index = comments.indexOfFirst { it.id == comment.id }
                        if (index != -1) {
                            comments[index] = comment.copy(content = newContent)
                            adapter.notifyItemChanged(index)
                        }
                        
                        // Limpiar el input y restaurar el listener original
                        commentInput.text.clear()
                        setupSendButton()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al actualizar el comentario", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun handleDeleteComment(comment: Comment) {
        db.collection("comentarios").document(comment.id)
            .update("activo", false)
            .addOnSuccessListener {
                val index = comments.indexOfFirst { it.id == comment.id }
                if (index != -1) {
                    comments.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    
                    // Actualizar contador de comentarios en el anuncio
                    announcementId?.let { id ->
                        // Obtener el número real de comentarios activos
                        db.collection("comentarios")
                            .whereEqualTo("comunicadoId", id)
                            .whereEqualTo("activo", true)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val realCount = querySnapshot.size()
                                // Actualizar el contador con el número real
                                db.collection("comunicados").document(id)
                                    .update("commentsCount", realCount)
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al eliminar el comentario", Toast.LENGTH_SHORT).show()
            }
    }
}
