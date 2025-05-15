package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar ProgressDialog
        progressDialog = ProgressDialog(this)

        // Referencia al botón personalizado (LinearLayout)
        val btnRecuperar = findViewById<LinearLayout>(R.id.btnRecuperar)

        // Referencia al campo de email
        val emailInput = findViewById<EditText>(R.id.emailInput)

        // Evento al hacer clic en "Recuperar"
        btnRecuperar.setOnClickListener {
            val email = emailInput.text.toString().trim()

            // Validar formato de correo
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Ingrese un correo electrónico válido"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            // Mostrar diálogo de progreso
            progressDialog.setMessage("Verificando correo...")
            progressDialog.show()

            buscarCorreoEnFirestore(email)
        }
    }

    private fun buscarCorreoEnFirestore(email: String) {
        Log.d("ForgotPassword", "Buscando correo en colección Estudiantes")
        db.collection("Estudiantes")
            .whereEqualTo("Correo", email)
            .get()
            .addOnSuccessListener { estudiantesResult ->
                if (!estudiantesResult.isEmpty) {
                    Log.d("ForgotPassword", "Correo encontrado en Estudiantes")
                    enviarCorreoRecuperacion(email)
                } else {
                    buscarEnProfesores(email)
                }
            }
            .addOnFailureListener { e ->
                manejarError("Error al buscar en Estudiantes", e)
            }
    }

    private fun buscarEnProfesores(email: String) {
        Log.d("ForgotPassword", "Buscando correo en colección Profesores")
        db.collection("Profesores")
            .whereEqualTo("Correo", email)
            .get()
            .addOnSuccessListener { profesoresResult ->
                if (!profesoresResult.isEmpty) {
                    Log.d("ForgotPassword", "Correo encontrado en Profesores")
                    enviarCorreoRecuperacion(email)
                } else {
                    progressDialog.dismiss()
                    Log.d("ForgotPassword", "Correo no encontrado en ninguna colección")
                    Toast.makeText(this, "El correo no está registrado. Por favor regístrese primero.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                manejarError("Error al buscar en Profesores", e)
            }
    }

    private fun enviarCorreoRecuperacion(email: String) {
        Log.d("ForgotPassword", "Enviando correo de recuperación directamente a: $email")
        // Intentar enviar el correo directamente sin verificar Auth
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d("ForgotPassword", "Correo de recuperación enviado exitosamente")
                Toast.makeText(this, "Se ha enviado un correo de recuperación a $email", Toast.LENGTH_LONG).show()
                irAPantallaConfirmacion()
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("no user record") == true) {
                    // Si el usuario no existe en Auth, crearlo
                    Log.d("ForgotPassword", "Usuario no existe en Auth, intentando crear...")
                    val tempPassword = "TempPass${System.currentTimeMillis()}"
                    
                    auth.createUserWithEmailAndPassword(email, tempPassword)
                        .addOnSuccessListener {
                            // Usuario creado, ahora enviar correo de recuperación
                            enviarCorreoRecuperacionDirecto(email)
                        }
                        .addOnFailureListener { createError ->
                            // Si falla la creación, probablemente el usuario ya existe
                            if (createError.message?.contains("email address is already") == true) {
                                // Intentar enviar el correo una vez más
                                enviarCorreoRecuperacionDirecto(email)
                            } else {
                                manejarError("Error al crear usuario", createError)
                            }
                        }
                } else {
                    manejarError("Error al enviar correo", e)
                }
            }
    }

    private fun irAPantallaConfirmacion() {
        val intent = Intent(this, ConfirmacionRecuperacionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun enviarCorreoRecuperacionDirecto(email: String) {
        Log.d("ForgotPassword", "Reintentando enviar correo de recuperación a: $email")
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d("ForgotPassword", "Correo de recuperación enviado exitosamente en el reintento")
                Toast.makeText(this, "Se ha enviado un correo de recuperación a $email", Toast.LENGTH_LONG).show()
                irAPantallaConfirmacion()
            }
            .addOnFailureListener { e ->
                manejarError("Error al reenviar correo", e)
            }
    }

    private fun manejarError(mensaje: String, e: Exception) {
        progressDialog.dismiss()
        Log.e("ForgotPassword", "$mensaje: ${e.message}")
        Toast.makeText(this, "$mensaje: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
