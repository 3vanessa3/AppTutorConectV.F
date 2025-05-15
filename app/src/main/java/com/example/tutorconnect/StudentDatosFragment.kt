package com.example.tutorconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentDatosFragment : Fragment() {
    private var isEditing = false
    private lateinit var viewMode: View
    private lateinit var editMode: View
    private lateinit var btnEditar: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_student_datos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener referencias a las vistas
        view.apply {
            viewMode = findViewById(R.id.viewMode)
            editMode = findViewById(R.id.editMode)
            btnEditar = findViewById(R.id.btnEditar)
            btnGuardar = findViewById(R.id.btnGuardar)
            btnCancelar = findViewById(R.id.btnCancelar)
        }

        // Configurar los botones
        btnEditar.setOnClickListener {
            startEditing()
        }

        btnGuardar.setOnClickListener {
            saveChanges()
        }

        btnCancelar.setOnClickListener {
            cancelEditing()
        }

        // Cargar datos iniciales
        loadData()
    }

    private fun loadData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("datos_Estudiante")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    view?.apply {
                        findViewById<TextView>(R.id.tvName)?.text = document.getString("Name") ?: ""
                        findViewById<TextView>(R.id.tvEmail)?.text = document.getString("Email") ?: ""
                        findViewById<TextView>(R.id.tvPhone)?.text = document.getString("Phone") ?: ""
                        findViewById<TextView>(R.id.tvAge)?.text = "Edad: ${document.getLong("Age")?.toString() ?: ""} años"
                        findViewById<TextView>(R.id.tvProgram)?.text = document.getString("Program") ?: ""
                        findViewById<TextView>(R.id.tvFavoriteSubjects)?.text = document.getString("FavoriteSubjects") ?: ""
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun startEditing() {
        isEditing = true
        viewMode.visibility = View.GONE
        editMode.visibility = View.VISIBLE
        
        // Copiar datos de TextViews a EditTexts
        view?.let { view ->
            view.findViewById<EditText>(R.id.etName)?.setText(view.findViewById<TextView>(R.id.tvName)?.text)
            view.findViewById<EditText>(R.id.etEmail)?.setText(view.findViewById<TextView>(R.id.tvEmail)?.text)
            view.findViewById<EditText>(R.id.etPhone)?.setText(view.findViewById<TextView>(R.id.tvPhone)?.text)
            view.findViewById<EditText>(R.id.etAge)?.setText(view.findViewById<TextView>(R.id.tvAge)?.text.toString().replace("Edad: ", "").replace(" años", ""))
            view.findViewById<EditText>(R.id.etProgram)?.setText(view.findViewById<TextView>(R.id.tvProgram)?.text)
            view.findViewById<EditText>(R.id.etFavoriteSubjects)?.setText(view.findViewById<TextView>(R.id.tvFavoriteSubjects)?.text)
        }
    }

    private fun saveChanges() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        view?.let { view ->
            val studentData = hashMapOf(
                "Name" to view.findViewById<EditText>(R.id.etName)?.text.toString(),
                "Email" to view.findViewById<EditText>(R.id.etEmail)?.text.toString(),
                "Phone" to view.findViewById<EditText>(R.id.etPhone)?.text.toString(),
                "Age" to (view.findViewById<EditText>(R.id.etAge)?.text.toString().toIntOrNull() ?: 0),
                "Program" to view.findViewById<EditText>(R.id.etProgram)?.text.toString(),
                "FavoriteSubjects" to view.findViewById<EditText>(R.id.etFavoriteSubjects)?.text.toString()
            )

            FirebaseFirestore.getInstance()
                .collection("datos_Estudiante")
                .document(currentUser.uid)
                .set(studentData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                    isEditing = false
                    viewMode.visibility = View.VISIBLE
                    editMode.visibility = View.GONE
                    loadData()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun cancelEditing() {
        isEditing = false
        viewMode.visibility = View.VISIBLE
        editMode.visibility = View.GONE
    }
}
