package com.example.tutorconnect.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorconnect.R
import com.example.tutorconnect.adapters.ProfessorsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class FavoritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddFavorite: FloatingActionButton
    private var allProfessors = listOf<Professor>()
    private var filteredProfessors = listOf<Professor>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerFavoritos)
        fabAddFavorite = view.findViewById(R.id.fabAddFavorite)

        setupFavoritosRecyclerView()
        setupFabButton()
        loadProfessors() // Cargar lista de profesores
    }

    private fun setupFavoritosRecyclerView() {
        // Ejemplo de datos de profesores favoritos
        val favoritos = mutableListOf(
            ProfesorFavorito(
                "Juan Pérez",
                "Álgebra Lineal, Cálculo",
                4.5f
            ),
            ProfesorFavorito(
                "María García",
                "Programación, Estructuras de Datos",
                5.0f
            )
        )

        recyclerView.adapter = FavoritosAdapter(favoritos)
    }

    private fun setupFabButton() {
        fabAddFavorite.setOnClickListener {
            showSearchProfessorsDialog()
        }
    }

    private fun showSearchProfessorsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_search_professors, null)
        
        val searchInput = dialogView.findViewById<TextInputEditText>(R.id.searchInput)
        val recyclerProfessors = dialogView.findViewById<RecyclerView>(R.id.recyclerProfessors)
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Configurar el adaptador para la lista de profesores
        val professorsAdapter = ProfessorsAdapter(filteredProfessors) { professor ->
            // Agregar profesor a favoritos
            addToFavorites(professor)
            dialog.dismiss()
        }
        recyclerProfessors.adapter = professorsAdapter

        // Configurar la búsqueda
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterProfessors(s.toString(), professorsAdapter)
            }
        })

        dialog.show()
    }

    private fun filterProfessors(query: String, adapter: ProfessorsAdapter) {
        filteredProfessors = if (query.isEmpty()) {
            allProfessors
        } else {
            allProfessors.filter { professor ->
                professor.name.contains(query, ignoreCase = true) ||
                professor.subjects.any { it.contains(query, ignoreCase = true) }
            }
        }
        adapter.updateProfessors(filteredProfessors)
    }

    private fun loadProfessors() {
        // Aquí cargarías los profesores desde tu base de datos
        // Por ahora usaremos datos de ejemplo
        allProfessors = listOf(
            Professor("1", "Juan Pérez", listOf("Álgebra Lineal", "Cálculo"), 4.5f),
            Professor("2", "María García", listOf("Programación", "Estructuras de Datos"), 5.0f),
            Professor("3", "Carlos López", listOf("Física", "Matemáticas"), 4.8f),
            Professor("4", "Ana Martínez", listOf("Química", "Biología"), 4.7f)
        )
        filteredProfessors = allProfessors
    }

    private fun addToFavorites(professor: Professor) {
        // Aquí implementarías la lógica para agregar el profesor a favoritos
        // Por ahora solo actualizamos la lista de favoritos
        val nuevoFavorito = ProfesorFavorito(
            professor.name,
            professor.subjects.joinToString(", "),
            professor.rating
        )
        
        // Actualizar el RecyclerView de favoritos
        val currentAdapter = recyclerView.adapter as FavoritosAdapter
        currentAdapter.addFavorito(nuevoFavorito)
    }
}

data class ProfesorFavorito(
    val nombre: String,
    val especialidades: String,
    val calificacion: Float
)

data class Professor(
    val id: String,
    val name: String,
    val subjects: List<String>,
    val rating: Float
)

class FavoritosAdapter(private val favoritos: MutableList<ProfesorFavorito>) :
    RecyclerView.Adapter<FavoritosAdapter.FavoritoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profesor_favorito, parent, false)
        return FavoritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        val favorito = favoritos[position]
        holder.bind(favorito)
    }

    override fun getItemCount() = favoritos.size

    fun addFavorito(favorito: ProfesorFavorito) {
        favoritos.add(favorito)
        notifyItemInserted(favoritos.size - 1)
    }

    class FavoritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        private val txtEspecialidades: TextView = view.findViewById(R.id.txtEspecialidades)
        private val txtCalificacion: TextView = view.findViewById(R.id.txtCalificacion)

        fun bind(favorito: ProfesorFavorito) {
            txtNombre.text = favorito.nombre
            txtEspecialidades.text = favorito.especialidades
            txtCalificacion.text = String.format("%.1f", favorito.calificacion)
        }
    }
}
