package com.example.tutorconnect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.model.StudyPlan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StudyPlanCreateStep2Activity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_plan_create_step2)

        db = FirebaseFirestore.getInstance()

        val planJson = intent.getStringExtra("plan")
        val type = object : TypeToken<StudyPlan>() {}.type
        val plan = Gson().fromJson<StudyPlan>(planJson, type)
        val timeInput = findViewById<EditText>(R.id.time)
        val topicsInput = findViewById<EditText>(R.id.topics)
        val tutoringInput = findViewById<EditText>(R.id.tutoringRequired)
        val backButton = findViewById<Button>(R.id.buttonBack)
        val nextButton = findViewById<Button>(R.id.buttonFinish)
        val errorText = findViewById<TextView>(R.id.errorText)

        backButton.setOnClickListener {
            finish()
        }

        nextButton.setOnClickListener {
            val weeklyHours = timeInput.text.toString().toIntOrNull() ?: 0
            val studyTopics = topicsInput.text.toString()
            val scheduledTutoring = tutoringInput.text.toString()

            if (weeklyHours > 0 && studyTopics.isNotBlank() && scheduledTutoring.isNotBlank()) {
                val updatedPlan = plan.copy(
                    weeklyHours = weeklyHours,
                    studyTopics = studyTopics,
                    scheduledTutoring = scheduledTutoring
                )

                // Save plan to Firebase
                db.collection("study_plans").document(updatedPlan.id)
                    .set(updatedPlan.toMap())
                    .addOnSuccessListener {
                        val resultIntent = Intent()
                        resultIntent.putExtra("plan", Gson().toJson(updatedPlan))
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        errorText.text = "Error al guardar el plan: ${e.message}"
                        errorText.visibility = View.VISIBLE
                    }
            } else {
                errorText.visibility = View.VISIBLE
            }
        }
    }
}