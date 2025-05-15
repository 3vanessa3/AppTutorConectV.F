package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TutorProfileActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor_profile)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Configurar el adaptador del ViewPager
        val pagerAdapter = TutorProfilePagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Conectar TabLayout con ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Datos"
                1 -> "Experiencia"
                2 -> "Calificación"
                else -> ""
            }
        }.attach()

        // Configurar botón de volver
        findViewById<ImageButton>(R.id.btnVolver)?.setOnClickListener {
            finish()
        }

        // Configurar botón de cerrar sesión
        findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            // Limpiar SharedPreferences
            val prefs = getSharedPreferences("TutorPrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()
            
            // Limpiar SharedPreferences de autenticación
            val authPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
            authPrefs.edit().clear().apply()
            
            // Redirigir a MainActivity (que contiene el login)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Configurar navegación inferior
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.nav_search).setOnClickListener {
            // TODO: Implementar navegación a búsqueda
        }

        // Ya estamos en el perfil, así que este botón no hace nada
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            // No hacemos nada, ya estamos en el perfil
        }
    }

    fun navigateToTab(position: Int) {
        viewPager.currentItem = position
    }
}

class TutorProfilePagerAdapter(fragmentActivity: FragmentActivity) : 
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TutorDatosFragment()
            1 -> TutorExperienciaFragment()
            2 -> ProfesorCalificacionesFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 