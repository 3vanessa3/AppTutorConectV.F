package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorconnect.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeFirebase()
        initializeViews()
        setupClickListeners()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
    }

    private fun setupClickListeners() {
        // Botón de registro
        findViewById<TextView>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Botón de recuperar contraseña
        findViewById<TextView>(R.id.txtForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Botón de login
        findViewById<LinearLayout>(R.id.btnLogin).setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        when {
            email.isEmpty() -> showFieldError(etEmail, "Ingrese su correo electrónico")
            password.isEmpty() -> showFieldError(etPassword, "Ingrese su contraseña")
            else -> performLogin(email, password)
        }
    }

    private fun showFieldError(field: EditText, message: String) {
        field.error = message
        field.requestFocus()
    }

    private fun performLogin(email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Iniciando sesión...")
        progressDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserTypeAndRedirect(progressDialog)
                } else {
                    progressDialog.dismiss()
                    handleLoginError(task.exception)
                }
            }
    }

    private fun checkUserTypeAndRedirect(progressDialog: ProgressDialog) {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("Profesores").document(userId).get()
            .addOnSuccessListener { tutorDoc ->
                progressDialog.dismiss()
                val targetActivity = if (tutorDoc.exists()) {
                    HomeActivity::class.java
                } else {
                    StudentHomeActivity::class.java
                }
                startActivity(Intent(this, targetActivity))
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("MainActivity", "Error al verificar tipo de usuario", e)
            }
    }

    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when (exception?.message) {
            null -> "Error de autenticación desconocido"
            else -> when {
                exception.message?.contains("password is invalid") == true -> {
                    showFieldError(etPassword, "Contraseña incorrecta")
                    "La contraseña ingresada es incorrecta"
                }
                exception.message?.contains("no user record") == true -> {
                    showFieldError(etEmail, "Usuario no registrado")
                    "Este correo no está registrado. Por favor regístrese primero."
                }
                else -> "Error: ${exception.message}"
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}
