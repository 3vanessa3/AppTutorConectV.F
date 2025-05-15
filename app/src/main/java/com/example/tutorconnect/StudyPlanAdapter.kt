package com.example.tutorconnect

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.model.StudyPlan
import com.example.tutorconnect.R
import com.example.tutorconnect.StudyPlanDetailActivity
import com.example.tutorconnect.StudyPlanCreateStep1Activity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.android.material.slider.Slider

class StudyPlanAdapter(
    private var plans: List<StudyPlan>,
    private val context: Context
) : RecyclerView.Adapter<StudyPlanAdapter.ViewHolder>() {

    interface OnPlanUpdateListener {
        fun onPlanUpdated(plan: StudyPlan)
    }

    private var updateListener: OnPlanUpdateListener? = null

    fun setOnPlanUpdateListener(listener: OnPlanUpdateListener) {
        this.updateListener = listener
    }

    private val db = FirebaseFirestore.getInstance()

    private fun showProgressUpdateDialog(plan: StudyPlan) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_update_progress)

        val progressSlider = dialog.findViewById<Slider>(R.id.progressSlider)
        val currentProgressText = dialog.findViewById<TextView>(R.id.currentProgressText)
        val selectedProgressText = dialog.findViewById<TextView>(R.id.selectedProgressText)
        val completeButton = dialog.findViewById<Button>(R.id.completeButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)

        // Configurar progreso actual
        progressSlider.value = plan.progress
        currentProgressText.text = "Progreso actual: ${(plan.progress * 100).toInt()}%"
        selectedProgressText.text = "Progreso seleccionado: ${(plan.progress * 100).toInt()}%"

        progressSlider.addOnChangeListener { _, value, _ ->
            selectedProgressText.text = "Progreso seleccionado: ${(value * 100).toInt()}%"
        }

        completeButton.setOnClickListener {
            updatePlanProgress(plan, 1f, true)
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            updatePlanProgress(plan, progressSlider.value, progressSlider.value >= 1f)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updatePlanProgress(plan: StudyPlan, progress: Float, completed: Boolean) {
        val updatedPlan = plan.copy(
            progress = progress,
            isCompleted = completed,
            lastUpdated = System.currentTimeMillis()
        )

        db.collection("Plan_de_estudios").document(plan.id)
            .set(updatedPlan.toMap())
            .addOnSuccessListener {
                Toast.makeText(context, "Progreso actualizado", Toast.LENGTH_SHORT).show()
                val position = plans.indexOfFirst { it.id == plan.id }
                if (position != -1) {
                    val newPlans = plans.toMutableList()
                    newPlans[position] = updatedPlan
                    plans = newPlans
                    notifyItemChanged(position)
                    updateListener?.onPlanUpdated(updatedPlan)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subject: TextView = itemView.findViewById(R.id.subject)
        val dates: TextView = itemView.findViewById(R.id.dates)
        val time: TextView = itemView.findViewById(R.id.time)
        val progress: TextView = itemView.findViewById(R.id.progress)
        val status: TextView = itemView.findViewById(R.id.status)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_study_plan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plan = plans[position]
        holder.subject.text = plan.subject
        holder.dates.text = "${plan.startDate} - ${plan.endDate}"
        holder.time.text = "${plan.weeklyHours} horas/semana"
        holder.progress.text = plan.getProgressPercentage()
        holder.status.text = if (plan.isCompleted) "Finalizado" else "En curso"
        holder.status.setTextColor(holder.itemView.context.getColor(
            if (plan.isCompleted) android.R.color.holo_green_dark else R.color.purple_dark
        ))

        holder.itemView.setOnClickListener {
            showProgressUpdateDialog(plan)
        }

        // Configurar el botón de edición
        holder.itemView.findViewById<View>(R.id.editButton)?.setOnClickListener {
            val intent = Intent(context, StudyPlanCreateStep1Activity::class.java)
            intent.putExtra("planId", plan.id)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            showPlanOptions(context, plan, holder)
            true
        }
    }

    private fun showPlanOptions(context: Context, plan: StudyPlan, holder: ViewHolder) {
        val popup = PopupMenu(context, holder.itemView)
        popup.menuInflater.inflate(R.menu.plan_options, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    val intent = Intent(context, StudyPlanCreateStep1Activity::class.java)
                    intent.putExtra("plan", Gson().toJson(plan))
                    context.startActivity(intent)
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation(context, plan, holder)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeleteConfirmation(context: Context, plan: StudyPlan, holder: ViewHolder) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar Plan")
            .setMessage("¿Estás seguro de que quieres eliminar este plan?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("Plan_de_estudios").document(plan.id)
                    .delete()
                    .addOnSuccessListener {
                        db.collection("Plan_de_estudios")
                            .get()
                            .addOnSuccessListener { documents ->
                                val newPlans = documents.mapNotNull { doc ->
                                    doc.toObject(StudyPlan::class.java)
                                }
                                updatePlans(newPlans)
                            }
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun getItemCount() = plans.size

    fun updatePlans(newPlans: List<StudyPlan>) {
        plans = newPlans
        notifyDataSetChanged()
    }
}