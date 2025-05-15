package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.models.SessionStatus
import com.example.tutorconnect.models.TutorRating
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TutorRatingActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentRating = 0
    private lateinit var starButtons: Array<ImageButton>
    private lateinit var feedbackInput: EditText
    private lateinit var btnEnviar: Button
    private lateinit var txtNombreTutor: TextView
    private lateinit var txtMateria: TextView
    private lateinit var txtFecha: TextView
    private var isViewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor_rating)

        initializeViews()
        setupSessionInfo()
        
        // Verificar si estamos en modo visualización
        val sessionStatus = intent.getStringExtra("SESSION_STATUS")
        isViewMode = sessionStatus == SessionStatus.RATED.toString()
        
        if (isViewMode) {
            setupViewMode()
        } else {
            setupEditMode()
        }

        // Configurar botón de volver
        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            // Si no estamos en modo visualización, marcar como pendiente de evaluación
            if (!isViewMode) {
                val resultIntent = Intent()
                resultIntent.putExtra("SESSION_ID", intent.getStringExtra("SESSION_ID"))
                resultIntent.putExtra("STATUS", SessionStatus.PENDING.toString())
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }
    }

    private fun initializeViews() {
        starButtons = arrayOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )
        feedbackInput = findViewById(R.id.editRetroalimentacion)
        btnEnviar = findViewById(R.id.btnEnviar)
        txtNombreTutor = findViewById(R.id.txtNombreTutor)
        txtMateria = findViewById(R.id.txtMateria)
        txtFecha = findViewById(R.id.txtFecha)
    }

    private fun setupSessionInfo() {
        txtNombreTutor.text = intent.getStringExtra("TUTOR_NAME")
        txtMateria.text = intent.getStringExtra("SUBJECT")
        txtFecha.text = intent.getStringExtra("DATE")
    }

    private fun setupViewMode() {
        // Deshabilitar interacción con estrellas y feedback
        starButtons.forEach { it.isEnabled = false }
        feedbackInput.isEnabled = false
        btnEnviar.visibility = View.GONE

        // Cargar calificación existente
        val rating = intent.getIntExtra("RATING", 0)
        val feedback = intent.getStringExtra("FEEDBACK") ?: ""
        
        updateRating(rating)
        feedbackInput.setText(feedback)
    }

    private fun setupEditMode() {
        // Configurar listeners para las estrellas
        starButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                updateRating(index + 1)
            }
        }

        // Configurar botón de enviar
        btnEnviar.setOnClickListener {
            if (currentRating > 0) {
                val rating = TutorRating(
                    sessionId = intent.getStringExtra("SESSION_ID") ?: "",
                    tutorName = intent.getStringExtra("TUTOR_NAME") ?: "",
                    subject = intent.getStringExtra("SUBJECT") ?: "",
                    date = intent.getStringExtra("DATE") ?: "",
                    rating = currentRating,
                    feedback = feedbackInput.text.toString()
                )
                saveRating(rating)
            } else {
                Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRating(rating: TutorRating) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val ratingData = hashMapOf(
            "professorId" to rating.sessionId,
            "studentId" to currentUser.uid,
            "studentName" to currentUser.displayName,
            "professorName" to rating.tutorName,
            "subject" to rating.subject,
            "rating" to rating.rating,
            "feedback" to rating.feedback,
            "date" to rating.date,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        // Guardar en Firestore
        db.collection("Evaluaciones")
            .add(ratingData)
            .addOnSuccessListener { documentReference ->
                // Crear intent con los resultados
                val resultIntent = Intent()
                resultIntent.putExtra("SESSION_ID", rating.sessionId)
                resultIntent.putExtra("RATING", rating.rating)
                resultIntent.putExtra("FEEDBACK", rating.feedback)
                resultIntent.putExtra("STATUS", SessionStatus.RATED.toString())
                
                // Establecer el resultado como OK y enviar los datos de vuelta
                setResult(RESULT_OK, resultIntent)
                
                Toast.makeText(this, "¡Calificación enviada!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar la calificación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateRating(rating: Int) {
        currentRating = rating
        // Actualizar la visualización de las estrellas
        starButtons.forEachIndexed { index, button ->
            button.setImageResource(
                if (index < rating) R.drawable.ic_star_filled
                else R.drawable.ic_star
            )
        }
    }
}
