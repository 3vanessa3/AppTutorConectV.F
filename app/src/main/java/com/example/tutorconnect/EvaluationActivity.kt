package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.adapters.SessionAdapter
import com.example.tutorconnect.models.SessionStatus
import com.example.tutorconnect.models.TutorRating
import com.example.tutorconnect.models.TutorSession
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EvaluationActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "EvaluationActivity"
        private const val RATING_REQUEST_CODE = 100
        private const val SELECT_TUTOR_REQUEST_CODE = 101
    }
    private lateinit var adapter: SessionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: ConstraintLayout
    private val sessions = mutableListOf<TutorSession>(
        TutorSession(
            id = "1",
            tutorName = "María González",
            subject = "Ecuaciones Lineales",
            date = "12/03/2024",
            status = SessionStatus.COMPLETED
        ),
        TutorSession(
            id = "2",
            tutorName = "Juan Pérez",
            subject = "Cálculo Diferencial",
            date = "15/03/2024",
            status = SessionStatus.RATED,
            rating = TutorRating(
                sessionId = "2",
                tutorName = "Juan Pérez",
                subject = "Cálculo Diferencial",
                date = "15/03/2024",
                rating = 5,
                feedback = "Excelente explicación de los conceptos difíciles. Muy paciente y claro."
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluation)

        initializeViews()
        setupRecyclerView()
        setupFab()
        updateEmptyState()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewTutors)
        emptyState = findViewById(R.id.emptyState)
    }

    private fun setupRecyclerView() {
        adapter = SessionAdapter { session ->
            when (session.status) {
                SessionStatus.COMPLETED -> navigateToRating(session)
                SessionStatus.RATED -> navigateToViewRating(session)
                else -> { /* No action for pending sessions */ }
            }
        }
        adapter.updateSessions(sessions)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        updateEmptyState()
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddRating).setOnClickListener {
            Log.d(TAG, "FAB clicked, abriendo SelectTutorActivity")
            startActivityForResult(Intent(this, SelectTutorActivity::class.java), SELECT_TUTOR_REQUEST_CODE)
        }
    }

    private fun navigateToRating(session: TutorSession) {
        val intent = Intent(this, TutorRatingActivity::class.java).apply {
            putExtra("TUTOR_NAME", session.tutorName)
            putExtra("SUBJECT", session.subject)
            putExtra("DATE", session.date)
            putExtra("SESSION_ID", session.id)
            putExtra("SESSION_STATUS", session.status.toString())
            // Agregar logs para debug
            Log.d("EvaluationActivity", "Navegando a calificación con: ${session.tutorName} - ${session.subject}")
        }
        startActivityForResult(intent, RATING_REQUEST_CODE)
    }

    private fun navigateToViewRating(session: TutorSession) {
        val rating = session.rating
        if (rating != null) {
            val intent = Intent(this, TutorRatingActivity::class.java).apply {
                putExtra("TUTOR_NAME", session.tutorName)
                putExtra("SUBJECT", session.subject)
                putExtra("DATE", session.date)
                putExtra("SESSION_ID", session.id)
                putExtra("SESSION_STATUS", session.status.toString())
                putExtra("RATING", rating.rating)
                putExtra("FEEDBACK", rating.feedback)
            }
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode, data=$data")
        
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                SELECT_TUTOR_REQUEST_CODE -> {
                    Log.d(TAG, "Procesando selección de tutor")
                    handleNewTutorSelection(data)
                }
                RATING_REQUEST_CODE -> {
                    Log.d(TAG, "Procesando resultado de calificación")
                    handleRatingResult(data)
                }
            }
        }
    }

    private fun handleNewTutorSelection(data: Intent) {
        val tutorName = data.getStringExtra("professorName") ?: return
        val subject = data.getStringExtra("subject") ?: return
        val date = data.getStringExtra("date") ?: ""
        val sessionId = data.getStringExtra("professorId") ?: return
        val status = SessionStatus.COMPLETED

        Log.d("EvaluationActivity", "Profesor seleccionado: $tutorName - $subject")

        // Crear nueva sesión
        val newSession = TutorSession(
            id = sessionId,
            tutorName = tutorName,
            subject = subject,
            date = date,
            status = status
        )

        // Agregar a la lista y actualizar UI
        sessions.add(newSession)
        adapter.updateSessions(sessions)
        updateEmptyState()

        // Iniciar actividad de calificación
        navigateToRating(newSession)
    }

    private fun handleRatingResult(data: Intent) {
        val sessionId = data.getStringExtra("SESSION_ID") ?: return
        val status = data.getStringExtra("STATUS")?.let { SessionStatus.valueOf(it) } ?: return
        
        // Encontrar y actualizar la sesión calificada
        val sessionIndex = sessions.indexOfFirst { it.id == sessionId }
        if (sessionIndex != -1) {
            val session = sessions[sessionIndex]
            sessions[sessionIndex] = when (status) {
                SessionStatus.RATED -> {
                    val rating = data.getIntExtra("RATING", 0)
                    val feedback = data.getStringExtra("FEEDBACK") ?: ""
                    session.copy(
                        status = status,
                        rating = TutorRating(
                            sessionId = sessionId,
                            tutorName = session.tutorName,
                            subject = session.subject,
                            date = session.date,
                            rating = rating,
                            feedback = feedback
                        )
                    )
                }
                SessionStatus.PENDING -> session.copy(status = status)
                else -> session
            }
            
            // Actualizar el adaptador
            adapter.updateSessions(sessions)
            updateEmptyState()
        }
    }

    private fun updateEmptyState() {
        if (sessions.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        loadRatings()
    }

    private fun loadRatings() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            updateEmptyState()
            return
        }

        db.collection("Evaluaciones")
            .whereEqualTo("studentId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                sessions.clear()
                for (document in documents) {
                    val session = TutorSession(
                        id = document.getString("professorId") ?: "",
                        tutorName = document.getString("professorName") ?: "",
                        subject = document.getString("subject") ?: "",
                        date = document.getString("date") ?: "",
                        status = SessionStatus.RATED,
                        rating = TutorRating(
                            sessionId = document.getString("professorId") ?: "",
                            tutorName = document.getString("professorName") ?: "",
                            subject = document.getString("subject") ?: "",
                            date = document.getString("date") ?: "",
                            rating = (document.get("rating") as? Number)?.toInt() ?: 0,
                            feedback = document.getString("feedback") ?: ""
                        )
                    )
                    sessions.add(session)
                }
                adapter.updateSessions(sessions)
                updateEmptyState()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                updateEmptyState()
            }
    }
}
