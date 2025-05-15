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

class TutorDatosFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_tutor_datos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener referencias a las vistas
        viewMode = view.findViewById(R.id.viewMode)
        editMode = view.findViewById(R.id.editMode)
        btnEditar = view.findViewById(R.id.btnEditar)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnCancelar = view.findViewById(R.id.btnCancelar)

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

    private fun startEditing() {
        isEditing = true
        viewMode.visibility = View.GONE
        editMode.visibility = View.VISIBLE
        
        // Copiar datos de TextViews a EditTexts
        view?.let { view ->
            view.findViewById<EditText>(R.id.etNombre)?.setText(view.findViewById<TextView>(R.id.tvNombre)?.text)
            view.findViewById<EditText>(R.id.etEdad)?.setText(view.findViewById<TextView>(R.id.tvEdad)?.text)
            view.findViewById<EditText>(R.id.etEspecialidad)?.setText(view.findViewById<TextView>(R.id.tvEspecialidad)?.text)
            view.findViewById<EditText>(R.id.etHorario)?.setText(view.findViewById<TextView>(R.id.tvHorario)?.text)
            view.findViewById<EditText>(R.id.etModalidades)?.setText(view.findViewById<TextView>(R.id.tvModalidades)?.text)
            view.findViewById<EditText>(R.id.etEmail)?.setText(view.findViewById<TextView>(R.id.tvEmail)?.text)
            view.findViewById<EditText>(R.id.etTelefono)?.setText(view.findViewById<TextView>(R.id.tvTelefono)?.text)
            view.findViewById<EditText>(R.id.etWhatsapp)?.setText(view.findViewById<TextView>(R.id.tvWhatsapp)?.text)
            view.findViewById<EditText>(R.id.etUbicacion)?.setText(view.findViewById<TextView>(R.id.tvUbicacion)?.text)
        }
    }

    private fun saveChanges() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener los valores de los EditText
        view?.let { view ->
            val tutorData = hashMapOf(
                "Nombre" to view.findViewById<EditText>(R.id.etNombre)?.text.toString(),
                "Edad" to view.findViewById<EditText>(R.id.etEdad)?.text.toString(),
                "Especialidad" to view.findViewById<EditText>(R.id.etEspecialidad)?.text.toString(),
                "Horario" to view.findViewById<EditText>(R.id.etHorario)?.text.toString(),
                "Modalidades" to view.findViewById<EditText>(R.id.etModalidades)?.text.toString(),
                "Email" to view.findViewById<EditText>(R.id.etEmail)?.text.toString(),
                "Telefono" to view.findViewById<EditText>(R.id.etTelefono)?.text.toString(),
                "Whatsapp" to view.findViewById<EditText>(R.id.etWhatsapp)?.text.toString(),
                "Ubicacion" to view.findViewById<EditText>(R.id.etUbicacion)?.text.toString()
            )

            // Guardar en Firebase
            FirebaseFirestore.getInstance()
                .collection("datos_profesor")
                .document(currentUser.uid)
                .set(tutorData)
                .addOnSuccessListener {
                    // Actualizar la UI
                    isEditing = false
                    viewMode.visibility = View.VISIBLE
                    editMode.visibility = View.GONE

                    // Actualizar TextViews
                    view.findViewById<TextView>(R.id.tvNombre)?.text = tutorData["Nombre"]
                    view.findViewById<TextView>(R.id.tvEdad)?.text = tutorData["Edad"]
                    view.findViewById<TextView>(R.id.tvEspecialidad)?.text = tutorData["Especialidad"]
                    view.findViewById<TextView>(R.id.tvHorario)?.text = tutorData["Horario"]
                    view.findViewById<TextView>(R.id.tvModalidades)?.text = tutorData["Modalidades"]
                    view.findViewById<TextView>(R.id.tvEmail)?.text = tutorData["Email"]
                    view.findViewById<TextView>(R.id.tvTelefono)?.text = tutorData["Telefono"]
                    view.findViewById<TextView>(R.id.tvWhatsapp)?.text = tutorData["Whatsapp"]
                    view.findViewById<TextView>(R.id.tvUbicacion)?.text = tutorData["Ubicacion"]

                    Toast.makeText(context, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
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

    private fun loadData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        view?.let { view ->
            FirebaseFirestore.getInstance()
                .collection("datos_profesor")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        view.findViewById<TextView>(R.id.tvNombre)?.text = document.getString("Nombre") ?: ""
                        view.findViewById<TextView>(R.id.tvEdad)?.text = document.getString("Edad") ?: ""
                        view.findViewById<TextView>(R.id.tvEspecialidad)?.text = document.getString("Especialidad") ?: ""
                        view.findViewById<TextView>(R.id.tvHorario)?.text = document.getString("Horario") ?: ""
                        view.findViewById<TextView>(R.id.tvModalidades)?.text = document.getString("Modalidades") ?: ""
                        view.findViewById<TextView>(R.id.tvEmail)?.text = document.getString("Email") ?: ""
                        view.findViewById<TextView>(R.id.tvTelefono)?.text = document.getString("Telefono") ?: ""
                        view.findViewById<TextView>(R.id.tvWhatsapp)?.text = document.getString("Whatsapp") ?: ""
                        view.findViewById<TextView>(R.id.tvUbicacion)?.text = document.getString("Ubicacion") ?: ""
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
} 