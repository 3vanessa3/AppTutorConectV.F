package com.example.tutorconnect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.model.StudyPlan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.tutorconnect.StudyPlanAdapter
import com.example.tutorconnect.utils.StudyPlanManager
import com.google.firebase.firestore.FirebaseFirestore

class StudyPlanListActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: StudyPlanAdapter
    companion object {
        private const val CREATE_PLAN_REQUEST = 1
        private const val DETAIL_PLAN_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_plan_list)

        db = FirebaseFirestore.getInstance()
        adapter = StudyPlanAdapter(emptyList(), this)
        adapter.setOnPlanUpdateListener(object : StudyPlanAdapter.OnPlanUpdateListener {
            override fun onPlanUpdated(plan: StudyPlan) {
                loadPlans()
            }
        })

        // Configurar botón de navegación
        findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            startActivity(Intent(this, StudentHomeActivity::class.java))
            finish()
        }

        // Configurar RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudyPlanAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        // Configurar TextView de "No hay planes"
        val noPlansText = findViewById<TextView>(R.id.noPlansText)
        noPlansText.visibility = View.GONE

        // Configurar FloatingActionButton
        val fab = findViewById<FloatingActionButton>(R.id.fabAddPlan)
        fab.setOnClickListener {
            startCreatePlan(it)
        }

        // Cargar planes
        loadPlans()
    }

    private fun loadPlans() {
        db.collection("Plan_de_estudios")
            .get()
            .addOnSuccessListener { documents ->
                val plans = documents.mapNotNull { doc ->
                    StudyPlan(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        planName = doc.getString("planName") ?: "",
                        subject = doc.getString("subject") ?: "",
                        objective = doc.getString("objective") ?: "",
                        startDate = doc.getString("startDate") ?: "",
                        endDate = doc.getString("endDate") ?: "",
                        weeklyHours = doc.getLong("weeklyHours")?.toInt() ?: 0,
                        studyTopics = doc.getString("studyTopics") ?: "",
                        scheduledTutoring = doc.getString("scheduledTutoring") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        progress = (doc.getDouble("progress") ?: 0.0).toFloat(),
                        lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()
                    )
                }
                adapter.updatePlans(plans)
                updateNoPlansVisibility()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar los planes: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun startCreatePlan(view: View) {
        val intent = Intent(this, StudyPlanCreateStep1Activity::class.java)
        startActivityForResult(intent, CREATE_PLAN_REQUEST)
    }

    private fun updateUI() {
        loadPlans()
    }

    private fun updateNoPlansVisibility() {
        val noPlansText = findViewById<TextView>(R.id.noPlansText)
        noPlansText.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun getPlans(): List<StudyPlan> {
        return StudyPlanManager.getPlans(this)
    }

    private fun handleNewPlan() {
        val intent = intent
        if (intent.hasExtra("new_plan")) {
            val planJson = intent.getStringExtra("new_plan")
            planJson?.let { json ->
                val plan = Gson().fromJson(json, StudyPlan::class.java)
                savePlan(plan)
                updateUI()
            }
        }
    }

    private fun savePlan(plan: StudyPlan) {
        StudyPlanManager.savePlan(this, plan)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CREATE_PLAN_REQUEST) {
            data?.getStringExtra("plan")?.let { planJson ->
                val updatedPlan = Gson().fromJson(planJson, StudyPlan::class.java)
                savePlan(updatedPlan)
                updateUI()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}