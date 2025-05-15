package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.adapters.ProfessorAdapter
import com.example.tutorconnect.models.Professor
import com.google.firebase.firestore.FirebaseFirestore

class TutorCalificacionFragment : Fragment() {
    companion object {
        private const val TAG = "TutorCalificacion"
    }
    private lateinit var adapter: ProfessorAdapter
    private val db = FirebaseFirestore.getInstance()
    private val professors = mutableListOf<Professor>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutor_calificacion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerProfessors)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ProfessorAdapter { professor ->
            // Navegar a la pantalla de calificación
            val intent = Intent(requireContext(), EvaluationActivity::class.java).apply {
                putExtra("professorId", professor.id)
                putExtra("professorName", professor.nombre)
                putExtra("subject", professor.especialidad)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Configurar la búsqueda
        view.findViewById<EditText>(R.id.searchInput).addTextChangedListener { text ->
            adapter.filterProfessors(text?.toString() ?: "")
        }

        // Cargar profesores
        loadProfessors()
    }

    private fun loadProfessors() {
        Log.d(TAG, "Iniciando carga de profesores")
        db.collection("Profesores").get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Documentos recibidos: ${documents.size()}")
                professors.clear()
                for (document in documents) {
                    Log.d(TAG, "Procesando documento ${document.id}")
                    professors.add(Professor(
                        id = document.id,
                        nombre = document.getString("Nombre_Completo") ?: "",
                        especialidad = document.getString("Especialidad") ?: "",
                        email = document.getString("Correo") ?: ""
                    ))
                }
                Log.d(TAG, "Total de profesores cargados: ${professors.size}")
                adapter.updateProfessors(professors)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al cargar profesores: ${e.message}")
                e.printStackTrace()
            }
    }
}

data class Calificacion(
    val nombreEstudiante: String,
    val calificacion: Float
)
