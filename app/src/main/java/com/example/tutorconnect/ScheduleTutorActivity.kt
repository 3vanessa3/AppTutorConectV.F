package com.example.tutorconnect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.adapters.AgendaAdapter
import com.example.tutorconnect.models.AgendaItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ScheduleTutorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AgendaAdapter
    private val agendaItems = mutableListOf<AgendaItem>()
    private val CREATE_AGENDA_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_tutor)

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewAgenda)

        // Configurar fecha actual
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("es"))
        findViewById<TextView>(R.id.txtFechaActual).text = dateFormat.format(Date())

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AgendaAdapter(agendaItems)
        recyclerView.adapter = adapter

        // Configurar botón de volver
        findViewById<ImageButton>(R.id.btnVolver)?.setOnClickListener {
            finish()
        }

        // Configurar botón flotante para agregar nueva tutoría
        findViewById<FloatingActionButton>(R.id.fabAddTutoria)?.setOnClickListener {
            val intent = Intent(this, CreateAgendaActivity::class.java)
            startActivityForResult(intent, CREATE_AGENDA_REQUEST)
        }

        // Configurar navegación inferior
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Ya estamos en la sección de tutorías, así que el botón home nos lleva al inicio
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.nav_search).setOnClickListener {
            // TODO: Implementar navegación a búsqueda
        }

        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, TutorProfileActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_AGENDA_REQUEST && resultCode == Activity.RESULT_OK) {
            val newAgendaItem = data?.getSerializableExtra("NEW_AGENDA_ITEM") as? AgendaItem
            if (newAgendaItem != null) {
                agendaItems.add(newAgendaItem)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO: Recargar las agendas cuando se vuelva a esta pantalla
    }
} 