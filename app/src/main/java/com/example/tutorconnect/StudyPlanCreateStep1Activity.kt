package com.example.tutorconnect

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.model.StudyPlan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class StudyPlanCreateStep1Activity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var editingPlan: StudyPlan? = null
    private var editingPlanId: String? = null
    
    private lateinit var weeklyHoursInput: TextInputEditText
    private lateinit var studyTopicsInput: TextInputEditText
    private lateinit var scheduledTutoringInput: TextInputEditText
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    companion object {
        private const val CREATE_PLAN_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_plan_create_step1)
        
        db = FirebaseFirestore.getInstance()
        editingPlanId = intent.getStringExtra("planId")
        
        if (editingPlanId != null) {
            loadPlanFromFirebase(editingPlanId!!)
        }
        
        // Initialize input fields
        weeklyHoursInput = findViewById(R.id.weeklyHours)
        studyTopicsInput = findViewById(R.id.studyTopics)
        scheduledTutoringInput = findViewById(R.id.scheduledTutoring)
        
        // Configurar la toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Crear plan de estudios"
            setDisplayHomeAsUpEnabled(true)
        }

        val nextButton = findViewById<Button>(R.id.buttonNext)
        val planName = findViewById<EditText>(R.id.planName)
        val subject = findViewById<EditText>(R.id.subject)
        val objective = findViewById<EditText>(R.id.objective)
        val startDate = findViewById<EditText>(R.id.startDate)
        val endDate = findViewById<EditText>(R.id.endDate)
        val weeklyHours = findViewById<EditText>(R.id.weeklyHours)
        val studyTopics = findViewById<EditText>(R.id.studyTopics)
        val scheduledTutoring = findViewById<EditText>(R.id.scheduledTutoring)
        val errorText = findViewById<TextView>(R.id.errorText)

        // Setup date pickers
        startDate.setOnClickListener { showDatePicker(startDate) }
        endDate.setOnClickListener { showDatePicker(endDate) }

        nextButton.setOnClickListener {
            val planNameText = planName.text.toString()
            val subjectText = subject.text.toString()
            val objectiveText = objective.text.toString()
            val startDateText = startDate.text.toString()
            val endDateText = endDate.text.toString()
            val weeklyHoursText = weeklyHours.text.toString()
            val studyTopicsText = studyTopics.text.toString()
            val scheduledTutoringText = scheduledTutoring.text.toString()

            if (validateInputs(planNameText, subjectText, objectiveText, startDateText, endDateText)) {
                val plan = StudyPlan(
                    id = editingPlanId ?: UUID.randomUUID().toString(),
                    planName = planNameText,
                    subject = subjectText,
                    objective = objectiveText,
                    startDate = startDateText,
                    endDate = endDateText,
                    weeklyHours = weeklyHoursText.toIntOrNull() ?: 0,
                    studyTopics = studyTopicsText,
                    scheduledTutoring = scheduledTutoringText,
                    isCompleted = false,
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    progress = 0f,
                    lastUpdated = System.currentTimeMillis()
                )

                savePlanToFirebase(plan)
            } else {
                errorText.visibility = View.VISIBLE
                errorText.text = "Por favor complete todos los campos requeridos"
            }
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            editText.setText(dateFormat.format(calendar.time))
        }, year, month, day).show()
    }

    private fun validateInputs(planName: String, subject: String, objective: String, startDate: String, endDate: String): Boolean {
        return planName.isNotEmpty() && 
               subject.isNotEmpty() && 
               objective.isNotEmpty() && 
               startDate.isNotEmpty() && 
               endDate.isNotEmpty()
    }

    private fun savePlanToFirebase(plan: StudyPlan) {
        db.collection("Plan_de_estudios")
            .document(plan.id)
            .set(plan.toMap())
            .addOnSuccessListener {
                Toast.makeText(this, "Plan de estudio creado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear el plan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadPlanFromFirebase(planId: String) {
        db.collection("Plan_de_estudios").document(planId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    editingPlan = StudyPlan(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        planName = document.getString("planName") ?: "",
                        subject = document.getString("subject") ?: "",
                        objective = document.getString("objective") ?: "",
                        startDate = document.getString("startDate") ?: "",
                        endDate = document.getString("endDate") ?: "",
                        weeklyHours = document.getLong("weeklyHours")?.toInt() ?: 0,
                        studyTopics = document.getString("studyTopics") ?: "",
                        scheduledTutoring = document.getString("scheduledTutoring") ?: "",
                        isCompleted = document.getBoolean("isCompleted") ?: false,
                        progress = (document.getDouble("progress") ?: 0.0).toFloat(),
                        lastUpdated = document.getLong("lastUpdated") ?: System.currentTimeMillis()
                    )
                    
                    // Fill form fields with plan data
                    findViewById<EditText>(R.id.planName).setText(editingPlan?.planName)
                    findViewById<EditText>(R.id.subject).setText(editingPlan?.subject)
                    findViewById<EditText>(R.id.objective).setText(editingPlan?.objective)
                    findViewById<EditText>(R.id.startDate).setText(editingPlan?.startDate)
                    findViewById<EditText>(R.id.endDate).setText(editingPlan?.endDate)
                    weeklyHoursInput.setText(editingPlan?.weeklyHours.toString())
                    studyTopicsInput.setText(editingPlan?.studyTopics)
                    scheduledTutoringInput.setText(editingPlan?.scheduledTutoring)
                } else {
                    Toast.makeText(this, "Plan no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar el plan: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CREATE_PLAN_REQUEST) {
            data?.getStringExtra("plan")?.let { planJson ->
                val updatedPlan = Gson().fromJson(planJson, StudyPlan::class.java)
                db.collection("Plan_de_estudios").document(updatedPlan.id)
                    .set(updatedPlan.toMap())
                    .addOnSuccessListener {
                        val intent = Intent()
                        intent.putExtra("plan", Gson().toJson(updatedPlan))
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar el plan: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}