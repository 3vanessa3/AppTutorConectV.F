package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.adapters.ProfessorAdapter
import com.example.tutorconnect.models.Professor
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class SelectTutorActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SelectTutor"
    }

    private lateinit var adapter: ProfessorAdapter
    private val db = FirebaseFirestore.getInstance()
    private val professors = mutableListOf<Professor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_tutor)

        // Configurar el RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerProfessors)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProfessorAdapter { professor ->
            val intent = Intent().apply {
                putExtra("professorId", professor.id)
                putExtra("professorName", professor.nombre)
                putExtra("subject", professor.especialidad)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
        recyclerView.adapter = adapter

        // Configurar la búsqueda
        findViewById<TextInputEditText>(R.id.searchInput).addTextChangedListener { text ->
            adapter.filterProfessors(text?.toString() ?: "")
        }

        // Cargar profesores
        loadProfessors()
    }

    private fun loadProfessors() {
        Log.d(TAG, "Iniciando carga de profesores")
        db.collection("datos_profesor").get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Documentos recibidos: ${documents.size()}")
                professors.clear()
                for (document in documents) {
                    Log.d(TAG, "Procesando documento ${document.id}")
                    professors.add(Professor(
                        id = document.id,
                        nombre = document.getString("Nombre") ?: "",
                        especialidad = document.getString("Especialidad") ?: "",
                        email = document.getString("Email") ?: ""
                    ))
                }
                Log.d(TAG, "Total de profesores cargados: ${professors.size}")
                adapter.updateProfessors(professors)

                // Mostrar u ocultar el mensaje de "no se encontraron profesores"
                val emptyView = findViewById<TextView>(R.id.emptyView)
                if (professors.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    emptyView.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al cargar profesores: ${e.message}")
                e.printStackTrace()
                // Mostrar el mensaje de error
                val emptyView = findViewById<TextView>(R.id.emptyView)
                emptyView.text = "Error al cargar los profesores"
                emptyView.visibility = View.VISIBLE
            }
    }
}
