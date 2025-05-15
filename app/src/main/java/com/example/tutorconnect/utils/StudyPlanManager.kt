package com.example.tutorconnect.utils

import android.content.Context
import com.example.tutorconnect.model.StudyPlan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot

object StudyPlanManager {
    private const val COLLECTION_NAME = "Plan_de_estudios"
    private val db = FirebaseFirestore.getInstance()

    fun getPlans(context: Context): List<StudyPlan> {
        val task = db.collection(COLLECTION_NAME).get()
        try {
            val snapshot = Tasks.await(task)
            return snapshot.documents.mapNotNull { doc ->
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
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun savePlan(context: Context, plan: StudyPlan) {
        db.collection(COLLECTION_NAME)
            .document(plan.id)
            .set(plan.toMap())
    }

    fun deletePlan(context: Context, planToDelete: StudyPlan) {
        db.collection(COLLECTION_NAME)
            .document(planToDelete.id)
            .delete()
    }
}
