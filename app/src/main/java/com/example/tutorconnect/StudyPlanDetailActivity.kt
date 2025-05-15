package com.example.tutorconnect

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.google.android.material.slider.Slider
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.annotation.IdRes
import com.example.tutorconnect.model.StudyPlan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class StudyPlanDetailActivity : AppCompatActivity() {
    private val editPlanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra("plan")?.let { planJson ->
                val updatedPlan = Gson().fromJson(planJson, StudyPlan::class.java)
                db.collection("study_plans").document(updatedPlan.id)
                    .set(updatedPlan.toMap())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Plan actualizado exitosamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar el plan: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
    private var plan: StudyPlan? = null
    private var dialog: AlertDialog? = null
    private var progressDialog: AlertDialog? = null
    private lateinit var db: FirebaseFirestore

    private inline fun <reified T : View> findViewByIdCompat(@IdRes id: Int): T {
        return findViewById(id)
    }

    companion object {
        private const val DETAIL_PLAN_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        db = FirebaseFirestore.getInstance()
        val planId = intent.getStringExtra("planId")
        
        if (planId != null) {
            loadPlanFromFirebase(planId)
        } else {
            val planJson = intent.getStringExtra("plan")
            plan = Gson().fromJson(planJson, StudyPlan::class.java)
            setupUI()
        }
        
        setContentView(R.layout.activity_study_plan_detail)
    }

    private fun setupUI() {
        val plan = plan ?: return
        
        setupToolbar()

        // Configurar los campos
        findViewById<TextView>(R.id.subjectText).text = plan.planName
        findViewById<TextView>(R.id.subjectText).text = plan.subject
        findViewById<TextView>(R.id.datesText).text = "${plan.startDate} - ${plan.endDate}"
        findViewById<TextView>(R.id.objectiveText).text = plan.objective
        findViewById<TextView>(R.id.studyTopicsText).text = plan.getFormattedTopics()
        findViewById<TextView>(R.id.weeklyHoursText).text = "${plan.weeklyHours} horas/semana"
        findViewById<TextView>(R.id.scheduledTutoringText).text = plan.scheduledTutoring
        findViewById<TextView>(R.id.progressDetail).text = plan.getProgressPercentage()
        findViewById<TextView>(R.id.lastUpdatedDetail).text = formatLastUpdated(plan.lastUpdated)

        // Configurar botones
        findViewById<FloatingActionButton>(R.id.fabEdit).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                val intent = Intent(this@StudyPlanDetailActivity, StudyPlanCreateStep1Activity::class.java)
                intent.putExtra("planId", plan.id)
                editPlanLauncher.launch(intent)
            }
        }

        // Remove Finalizar plan button since it's redundant

        findViewById<Button>(R.id.btnUpdateProgress).setOnClickListener {
            showUpdateProgressDialog()
        }

        findViewById<FloatingActionButton>(R.id.fabDelete).setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Plan")
            .setMessage("¿Estás seguro de que quieres eliminar este plan?")
            .setPositiveButton("Eliminar") { _, _ ->
                plan?.let { deletedPlan ->
                    db.collection("study_plans").document(deletedPlan.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Plan eliminado exitosamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al eliminar el plan: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun formatLastUpdated(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = plan?.subject
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun showUpdateProgressDialog() {
        val plan = plan ?: return
        
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_update_progress, null)

        val progressSlider = dialogView.findViewById<Slider>(R.id.progressSlider)
        val currentProgressText = dialogView.findViewById<TextView>(R.id.currentProgressText)
        val selectedProgressText = dialogView.findViewById<TextView>(R.id.selectedProgressText)
        val completeButton = dialogView.findViewById<Button>(R.id.completeButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        // Configurar el Slider
        progressSlider.value = plan?.progress ?: 0f
        currentProgressText.text = "Progreso actual: ${(plan?.progress?.times(100) ?: 0).toInt()}%"
        selectedProgressText.text = "Progreso seleccionado: ${(progressSlider.value * 100).toInt()}%"

        // Configurar botones
        completeButton.setOnClickListener {
            plan?.let { updatedPlan ->
                updatedPlan.markAsCompleted()
                db.collection("study_plans").document(updatedPlan.id)
                    .set(updatedPlan.toMap())
                    .addOnSuccessListener {
                        Toast.makeText(this@StudyPlanDetailActivity, "Plan completado exitosamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@StudyPlanDetailActivity, "Error al completar el plan: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        progressSlider.addOnChangeListener { _, value, _ ->
            selectedProgressText.text = "Progreso seleccionado: ${(value * 100).toInt()}%"
        }

        // Configurar botones
        completeButton.setOnClickListener {
            plan?.let { currentPlan ->
                updatePlanProgress(currentPlan, 1f, true)
                progressDialog?.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            progressDialog?.dismiss()
        }

        saveButton.setOnClickListener {
            plan?.let { currentPlan ->
                updatePlanProgress(currentPlan, progressSlider.value, progressSlider.value >= 1f)
                progressDialog?.dismiss()
            }
        }

        val builder = AlertDialog.Builder(this@StudyPlanDetailActivity)
            .setView(dialogView)
            .create()

        progressDialog = builder
        progressDialog?.show()
    }

    private fun updatePlanProgress(plan: StudyPlan, progress: Float, completed: Boolean) {
        val updatedPlan = plan.copy(
            progress = progress,
            isCompleted = completed,
            lastUpdated = System.currentTimeMillis()
        )

        db.collection("Plan_de_estudios").document(updatedPlan.id)
            .set(updatedPlan.toMap())
            .addOnSuccessListener {
                Toast.makeText(this, "Progreso actualizado exitosamente", Toast.LENGTH_SHORT).show()
                this.plan = updatedPlan
                setupUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar el progreso: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadPlanFromFirebase(planId: String) {
        db.collection("Plan_de_estudios").document(planId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    plan = StudyPlan(
                        id = document.id,
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
                    setupUI()
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



    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
