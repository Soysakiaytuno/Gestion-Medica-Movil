package com.example.appmedica.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.appmedica.db.DBClass
import com.example.appmedica.model.Paciente

class BuscarPacienteViewModel : ViewModel() {

    var query by mutableStateOf("")
        private set

    fun onQueryChange(nuevoTexto: String) {
        query = nuevoTexto
    }

    fun pacientesFiltrados(): List<Paciente> {
        val busqueda = query.trim()
        val todos = DBClass.getPacientes()
        if (busqueda.isEmpty()) return todos

        return todos.filter {
            "${it.nombre} ${it.apellidoPaterno} ${it.apellidoMaterno}"
                .contains(busqueda, ignoreCase = true)
        }
    }
}