package com.example.tutorconnect

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearComunicadoActivity : AppCompatActivity() {

    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var spinnerTipo: Spinner
    private lateinit var btnCrear: LinearLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_comunicado)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Verificar autenticación
        if (auth.currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        android.util.Log.d("CrearComunicado", "Usuario autenticado: ${auth.currentUser?.uid}")

        // Inicializar vistas
        etTitulo = findViewById(R.id.etTitulo)
        etDescripcion = findViewById(R.id.etDescripcion)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        btnCrear = findViewById(R.id.btnCrear)

        // Configurar spinner
        val tipos = arrayOf("EVENT", "COURSE", "NEWS", "NOTICE")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adapter

        // Configurar botón crear
        btnCrear.setOnClickListener {
            crearComunicado()
        }
    }

    private fun crearComunicado() {
        val titulo = etTitulo.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val tipo = spinnerTipo.selectedItem.toString()

        when {
            titulo.isEmpty() -> {
                etTitulo.error = "Ingrese un título"
                etTitulo.requestFocus()
                return
            }
            descripcion.isEmpty() -> {
                etDescripcion.error = "Ingrese una descripción"
                etDescripcion.requestFocus()
                return
            }
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creando comunicado...")
        progressDialog.show()

        // Obtener información del usuario actual
        val userId = auth.currentUser?.uid
        if (userId == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            return
        }

        // Obtener el nombre del profesor
        val userDocRef = db.collection("Profesores").document(auth.currentUser!!.uid)
        android.util.Log.d("CrearComunicado", "Buscando profesor con ID: ${auth.currentUser!!.uid}")
        
        userDocRef.get()
            .addOnSuccessListener { document ->
                android.util.Log.d("CrearComunicado", "¿Documento existe?: ${document.exists()}")
                
                val autorNombre = if (document.exists()) {
                    android.util.Log.d("CrearComunicado", "Documento encontrado: ${document.data}")
                    document.getString("Nombre_Completo").also { nombre ->
                        android.util.Log.d("CrearComunicado", "Nombre obtenido: $nombre")
                        if (nombre == null) {
                            android.util.Log.e("CrearComunicado", "Campo Nombre_Completo no encontrado en el documento")
                        }
                    }
                } else {
                    android.util.Log.e("CrearComunicado", "Documento del profesor no encontrado")
                    null
                }
                
                val autorNombreFinal = autorNombre ?: "Nombre del Tutor"
                
                // Crear referencia al documento con ID autogenerado
                val docRef = db.collection("comunicados").document()
                
                // Crear el mapa de datos del comunicado
                val comunicadoMap = hashMapOf(
                    "autorNombre" to autorNombreFinal,
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "tipo" to tipo
                )

                // Guardar en Firestore usando el ID autogenerado
                docRef.set(comunicadoMap)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Comunicado creado exitosamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error al crear comunicado: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al obtener información del usuario: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
