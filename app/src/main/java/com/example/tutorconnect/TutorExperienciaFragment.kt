package com.example.tutorconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TutorExperienciaFragment : Fragment() {
    private lateinit var viewMode: LinearLayout
    private lateinit var editMode: LinearLayout
    private lateinit var btnEditarExperiencia: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnAgregarExperiencia: Button

    // TextViews del modo visualización
    private lateinit var tvTitulo1: TextView
    private lateinit var tvDescripcion1: TextView
    private lateinit var tvTitulo2: TextView
    private lateinit var tvDescripcion2: TextView
    private lateinit var tvTitulo3: TextView
    private lateinit var tvDescripcion3: TextView

    // EditTexts del modo edición
    private lateinit var etTitulo1: EditText
    private lateinit var etDescripcion1: EditText
    private lateinit var etTitulo2: EditText
    private lateinit var etDescripcion2: EditText
    private lateinit var etTitulo3: EditText
    private lateinit var etDescripcion3: EditText

    // Control de experiencias
    private var experienciaActual = 1 // Iniciamos con una experiencia visible
    private val maxExperiencias = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tutor_experiencia, container, false)

        // Inicializar vistas
        initializeViews(view)
        setupListeners()
        loadData()

        return view
    }

    private fun initializeViews(view: View) {
        // Modos de vista
        viewMode = view.findViewById(R.id.viewMode)
        editMode = view.findViewById(R.id.editMode)

        // Botones
        btnEditarExperiencia = view.findViewById(R.id.btnEditarExperiencia)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnCancelar = view.findViewById(R.id.btnCancelar)
        btnAgregarExperiencia = view.findViewById(R.id.btnAgregarExperiencia)

        // TextViews del modo visualización
        tvTitulo1 = view.findViewById(R.id.tvTitulo1)
        tvDescripcion1 = view.findViewById(R.id.tvDescripcion1)
        tvTitulo2 = view.findViewById(R.id.tvTitulo2)
        tvDescripcion2 = view.findViewById(R.id.tvDescripcion2)
        tvTitulo3 = view.findViewById(R.id.tvTitulo3)
        tvDescripcion3 = view.findViewById(R.id.tvDescripcion3)

        // EditTexts del modo edición
        etTitulo1 = view.findViewById(R.id.etTitulo1)
        etDescripcion1 = view.findViewById(R.id.etDescripcion1)
        etTitulo2 = view.findViewById(R.id.etTitulo2)
        etDescripcion2 = view.findViewById(R.id.etDescripcion2)
        etTitulo3 = view.findViewById(R.id.etTitulo3)
        etDescripcion3 = view.findViewById(R.id.etDescripcion3)
    }

    private fun setupListeners() {
        btnEditarExperiencia.setOnClickListener {
            switchToEditMode()
        }

        btnGuardar.setOnClickListener {
            if (validarCampos()) {
                saveChanges()
                switchToViewMode()
            }
        }

        btnCancelar.setOnClickListener {
            resetearCampos()
            switchToViewMode()
        }

        btnAgregarExperiencia.setOnClickListener {
            agregarNuevaExperiencia()
        }
    }

    private fun switchToEditMode() {
        viewMode.visibility = View.GONE
        editMode.visibility = View.VISIBLE

        // Copiar contenido de TextViews a EditTexts
        etTitulo1.setText(tvTitulo1.text)
        etDescripcion1.setText(tvDescripcion1.text)
        etTitulo2.setText(tvTitulo2.text)
        etDescripcion2.setText(tvDescripcion2.text)
        etTitulo3.setText(tvTitulo3.text)
        etDescripcion3.setText(tvDescripcion3.text)

        // Mostrar solo las experiencias actuales
        actualizarVisibilidadCampos()
    }

    private fun switchToViewMode() {
        viewMode.visibility = View.VISIBLE
        editMode.visibility = View.GONE
    }

    private fun saveChanges() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val experiencias = mutableListOf<Map<String, String>>()

        // Agregar primera experiencia (obligatoria)
        experiencias.add(mapOf(
            "titulo" to etTitulo1.text.toString(),
            "descripcion" to etDescripcion1.text.toString()
        ))

        // Agregar segunda experiencia si existe
        if (experienciaActual >= 2 && etTitulo2.text.isNotBlank()) {
            experiencias.add(mapOf(
                "titulo" to etTitulo2.text.toString(),
                "descripcion" to etDescripcion2.text.toString()
            ))
        }

        // Agregar tercera experiencia si existe
        if (experienciaActual >= 3 && etTitulo3.text.isNotBlank()) {
            experiencias.add(mapOf(
                "titulo" to etTitulo3.text.toString(),
                "descripcion" to etDescripcion3.text.toString()
            ))
        }

        // Guardar en Firebase
        FirebaseFirestore.getInstance()
            .collection("Experiencia_Profesor")
            .document(currentUser.uid)
            .collection("experiencias")
            .get()
            .addOnSuccessListener { snapshot ->
                // Eliminar documentos existentes
                val batch = FirebaseFirestore.getInstance().batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // Agregar nuevos documentos
                experiencias.forEachIndexed { index, exp ->
                    val docRef = FirebaseFirestore.getInstance()
                        .collection("Experiencia_Profesor")
                        .document(currentUser.uid)
                        .collection("experiencias")
                        .document("experiencia_${index + 1}")
                    batch.set(docRef, exp)
                }

                // Ejecutar el batch
                batch.commit()
                    .addOnSuccessListener {
                        // Actualizar TextViews
                        tvTitulo1.text = etTitulo1.text.toString()
                        tvDescripcion1.text = etDescripcion1.text.toString()
                        tvTitulo2.text = etTitulo2.text.toString()
                        tvDescripcion2.text = etDescripcion2.text.toString()
                        tvTitulo3.text = etTitulo3.text.toString()
                        tvDescripcion3.text = etDescripcion3.text.toString()

                        Toast.makeText(context, "Experiencias guardadas correctamente", Toast.LENGTH_SHORT).show()
                        switchToViewMode()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al acceder a las experiencias: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun agregarNuevaExperiencia() {
        if (experienciaActual < maxExperiencias) {
            experienciaActual++
            when (experienciaActual) {
                2 -> {
                    etTitulo2.visibility = View.VISIBLE
                    etDescripcion2.visibility = View.VISIBLE
                }
                3 -> {
                    etTitulo3.visibility = View.VISIBLE
                    etDescripcion3.visibility = View.VISIBLE
                    btnAgregarExperiencia.visibility = View.GONE // Ocultar botón al llegar al máximo
                }
            }
        }
    }

    private fun actualizarVisibilidadCampos() {
        // Ocultar todos los campos primero
        etTitulo2.visibility = View.GONE
        etDescripcion2.visibility = View.GONE
        etTitulo3.visibility = View.GONE
        etDescripcion3.visibility = View.GONE
        btnAgregarExperiencia.visibility = View.VISIBLE

        // Mostrar campos según experienciaActual
        when (experienciaActual) {
            2 -> {
                etTitulo2.visibility = View.VISIBLE
                etDescripcion2.visibility = View.VISIBLE
            }
            3 -> {
                etTitulo2.visibility = View.VISIBLE
                etDescripcion2.visibility = View.VISIBLE
                etTitulo3.visibility = View.VISIBLE
                etDescripcion3.visibility = View.VISIBLE
                btnAgregarExperiencia.visibility = View.GONE
            }
        }
    }

    private fun validarCampos(): Boolean {
        var valido = true
        var mensaje = ""

        // Validar primera experiencia (siempre requerida)
        if (etTitulo1.text.isNullOrBlank() || etDescripcion1.text.isNullOrBlank()) {
            mensaje = "La primera experiencia es obligatoria"
            valido = false
        }

        // Validar experiencias adicionales solo si están visibles
        if (experienciaActual >= 2 && (etTitulo2.text.isNullOrBlank() || etDescripcion2.text.isNullOrBlank())) {
            mensaje = "Complete todos los campos de la segunda experiencia"
            valido = false
        }

        if (experienciaActual >= 3 && (etTitulo3.text.isNullOrBlank() || etDescripcion3.text.isNullOrBlank())) {
            mensaje = "Complete todos los campos de la tercera experiencia"
            valido = false
        }

        if (!valido) {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }

        return valido
    }

    private fun resetearCampos() {
        experienciaActual = 1
        etTitulo1.setText(tvTitulo1.text)
        etDescripcion1.setText(tvDescripcion1.text)
        etTitulo2.setText(tvTitulo2.text)
        etDescripcion2.setText(tvDescripcion2.text)
        etTitulo3.setText(tvTitulo3.text)
        etDescripcion3.setText(tvDescripcion3.text)
    }

    private fun loadData() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("Experiencia_Profesor")
            .document(currentUser.uid)
            .collection("experiencias")
            .get()
            .addOnSuccessListener { snapshot ->
                experienciaActual = 1
                
                // Limpiar todos los campos
                tvTitulo1.text = ""
                tvDescripcion1.text = ""
                tvTitulo2.text = ""
                tvDescripcion2.text = ""
                tvTitulo3.text = ""
                tvDescripcion3.text = ""

                // Ordenar documentos por ID para mantener el orden
                val documentos = snapshot.documents.sortedBy { it.id }

                documentos.forEachIndexed { index, doc ->
                    when (index) {
                        0 -> {
                            tvTitulo1.text = doc.getString("titulo") ?: ""
                            tvDescripcion1.text = doc.getString("descripcion") ?: ""
                            experienciaActual = 1
                        }
                        1 -> {
                            tvTitulo2.text = doc.getString("titulo") ?: ""
                            tvDescripcion2.text = doc.getString("descripcion") ?: ""
                            experienciaActual = 2
                        }
                        2 -> {
                            tvTitulo3.text = doc.getString("titulo") ?: ""
                            tvDescripcion3.text = doc.getString("descripcion") ?: ""
                            experienciaActual = 3
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al cargar experiencias: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
} 