package com.example.appmedica.db

import com.example.appmedica.model.Paciente

object DBClass {
    private val listaDePacientes = mutableListOf<Paciente>()

    fun agregar(newPaciente: Paciente) {
        listaDePacientes.add(newPaciente)
    }

    fun getPaciente(nombre: String): Paciente? {
        return listaDePacientes.find { it.nombre == nombre }
    }

    fun getPacientes(): List<Paciente> {
        return listaDePacientes.toList()
    }
}