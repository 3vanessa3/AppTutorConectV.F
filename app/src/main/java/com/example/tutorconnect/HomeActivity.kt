package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.adapters.AgendaAdapter
import com.example.tutorconnect.models.AgendaItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AgendaAdapter
    private val agendaItems = mutableListOf<AgendaItem>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Cargar el nombre del usuario
        loadUserName()

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAgenda)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AgendaAdapter(agendaItems)
        recyclerView.adapter = adapter

        // Configurar el botón de programar evento
        findViewById<EditText>(R.id.btnProgramarEvento).setOnClickListener {
            startActivity(Intent(this, ScheduleTutorActivity::class.java))
        }

        // Configurar clic en la tarjeta de programar tutorías
        findViewById<CardView>(R.id.cardProgramarTutorias).setOnClickListener {
            startActivity(Intent(this, ScheduleTutorActivity::class.java))
        }

        // Configurar clic en la tarjeta de comunicados
        findViewById<CardView>(R.id.cardComunicados).setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        // Configurar navegación inferior
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            // Ya estamos en home
        }

        findViewById<ImageView>(R.id.nav_search).setOnClickListener {
            startActivity(Intent(this, ScheduleTutorActivity::class.java))
        }

        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, TutorProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la lista de agenda cuando la actividad se reanuda
        updateAgendaList()
    }

    private fun loadUserName() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("Profesores").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("Nombre_Completo") ?: ""
                        val greeting = "¡Hola $nombre!"
                        findViewById<TextView>(R.id.greetingText).text = greeting
                    }
                }
        }
    }

    private fun updateAgendaList() {
        // TODO: Reemplazar con datos reales de la base de datos
        // Por ahora usamos datos de ejemplo
        val newAgendaItem = intent.getSerializableExtra("NEW_AGENDA_ITEM") as? AgendaItem
        if (newAgendaItem != null) {
            agendaItems.add(newAgendaItem)
            adapter.notifyDataSetChanged()
        }
    }
}
