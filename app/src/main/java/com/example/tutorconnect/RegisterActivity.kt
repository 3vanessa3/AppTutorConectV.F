package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.tutorconnect.utils.ProgressDialog

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Referencias a los views
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val switchTutor = findViewById<Switch>(R.id.switchTutor)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Animación opcional para el botón
        val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce)

        btnRegister.setOnClickListener {
            btnRegister.startAnimation(bounceAnim) // animación rebote

            val nombre = etName.text.toString().trim()
            val correo = etEmail.text.toString().trim()
            val telefono = etPhone.text.toString().trim()
            val contraseña = etPassword.text.toString()
            val esTutor = switchTutor.isChecked

            // Validaciones básicas
            when {
                nombre.isEmpty() -> {
                    etName.error = "Nombre requerido"
                    etName.requestFocus()
                    return@setOnClickListener
                }
                correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                    etEmail.error = "Correo inválido"
                    etEmail.requestFocus()
                    return@setOnClickListener
                }
                telefono.isEmpty() || telefono.length < 8 -> {
                    etPhone.error = "Teléfono inválido"
                    etPhone.requestFocus()
                    return@setOnClickListener
                }
                contraseña.isEmpty() || contraseña.length < 6 -> {
                    etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                    etPassword.requestFocus()
                    return@setOnClickListener
                }
                else -> {
                    // Mostrar progreso
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Registrando usuario...")
                    progressDialog.show()

                    // Crear usuario en Firebase Auth
                    auth.createUserWithEmailAndPassword(correo, contraseña)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Obtener el ID del usuario creado
                                val userId = auth.currentUser?.uid
                                
                                // Crear documento en Firestore
                                val userData = hashMapOf(
                                    "Nombre_Completo" to nombre,
                                    "Correo" to correo,
                                    "Telefono" to telefono,
                                    "Tipo" to if (esTutor) "tutor" else "estudiante"
                                )

                                if (userId != null) {
                                    // Guardar en la colección correspondiente
                                    val collection = if (esTutor) "Profesores" else "Estudiantes"
                                    Log.d("RegisterActivity", "Usuario creado en Auth con ID: $userId")
                                    Log.d("RegisterActivity", "Guardando en colección: $collection")
                                    Log.d("RegisterActivity", "Datos a guardar: $userData")
                                    
                                    db.collection(collection)
                                        .document(userId)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            progressDialog.dismiss()
                                            Log.d("RegisterActivity", "Datos guardados exitosamente en Firestore")
                                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            progressDialog.dismiss()
                                            Log.e("RegisterActivity", "Error al guardar en Firestore: ${e.message}")
                                            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                                            Log.e("RegisterActivity", "Error al guardar en Firestore", e)
                                        }
                                }
                            } else {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                Log.e("RegisterActivity", "Error en autenticación", task.exception)
                            }
                        }
                }
            }
        }
    }
}


