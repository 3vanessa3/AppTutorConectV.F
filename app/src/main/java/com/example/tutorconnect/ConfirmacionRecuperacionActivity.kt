package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ConfirmacionRecuperacionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_recuperacion)

        val btnVolver = findViewById<LinearLayout>(R.id.btnVolver)
        btnVolver.setOnClickListener {
            // Volver a la pantalla de inicio y limpiar el stack de actividades
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
