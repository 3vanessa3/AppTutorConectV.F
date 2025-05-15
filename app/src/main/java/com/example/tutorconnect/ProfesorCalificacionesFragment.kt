package com.example.tutorconnect

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.adapters.CalificacionesAdapter
import com.example.tutorconnect.models.Calificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfesorCalificacionesFragment : Fragment() {
    companion object {
        private const val TAG = "ProfesorCalificaciones"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: CalificacionesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profesor_calificaciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerCalificaciones)
        emptyView = view.findViewById(R.id.emptyView)

        setupRecyclerView()
        loadCalificaciones()
    }

    private fun setupRecyclerView() {
        adapter = CalificacionesAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun loadCalificaciones() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado")
            updateEmptyState(true)
            return
        }

        Log.d(TAG, "Usuario actual: ${currentUser.email}")

        // Primero obtener el ID del profesor
        db.collection("datos_profesor")
            .whereEqualTo("Email", currentUser.email)
            .get()
            .addOnSuccessListener { profesorDocs ->
                if (profesorDocs.isEmpty) {
                    Log.e(TAG, "No se encontró el profesor con email: ${currentUser.email}")
                    updateEmptyState(true)
                    return@addOnSuccessListener
                }

                val profesorDoc = profesorDocs.documents[0]
                val profesorId = profesorDoc.id
                Log.d(TAG, "ID del profesor encontrado: $profesorId")

                // Buscar evaluaciones usando el ID del profesor
                db.collection("Evaluaciones")
                    .whereEqualTo("professorId", profesorId)
                    .get()
                    .addOnSuccessListener { documents ->
                        Log.d(TAG, "Evaluaciones encontradas: ${documents.size()}")
                        val calificaciones = documents.mapNotNull { doc ->
                            try {
                                Log.d(TAG, "Procesando evaluación: ${doc.id} con datos: ${doc.data}")
                                Calificacion(
                                    id = doc.id,
                                    nombreEstudiante = doc.getString("studentName") ?: "",
                                    calificacion = doc.getLong("rating")?.toFloat() ?: 0f,
                                    feedback = doc.getString("feedback") ?: "",
                                    fecha = doc.getString("date") ?: "",
                                    profesorId = doc.getString("professorId") ?: "",
                                    estudianteId = doc.getString("studentId") ?: ""
                                ).also { cal ->
                                    Log.d(TAG, "Calificación creada: $cal")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al procesar documento ${doc.id}", e)
                                null
                            }
                        }
                        
                        Log.d(TAG, "Total de calificaciones procesadas: ${calificaciones.size}")
                        updateEmptyState(calificaciones.isEmpty())
                        adapter.submitList(calificaciones)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al cargar evaluaciones", e)
                        updateEmptyState(true)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al buscar profesor", e)
                updateEmptyState(true)
            }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}
