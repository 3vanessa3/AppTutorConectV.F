package com.example.tutorconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.tutorconnect.fragments.AcademicProgressFragment
import com.example.tutorconnect.fragments.FavoritesFragment

class StudentProfileActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        // Configurar botón de navegación
        findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            startActivity(Intent(this, StudentHomeActivity::class.java))
            finish()
        }

        // Configurar botón de cerrar sesión
        findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            // Limpiar SharedPreferences
            val prefs = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
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

        // Configurar navegación
        setupNavigation()
    }

    private fun setupNavigation() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Configurar ViewPager
        viewPager.adapter = ProfilePagerAdapter(this)

        // Conectar TabLayout con ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Datos"
                1 -> "Progreso académico"
                2 -> "Favoritos"
                else -> ""
            }
        }.attach()
    }
}

class ProfilePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StudentDatosFragment()
            1 -> AcademicProgressFragment()
            2 -> FavoritesFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}
