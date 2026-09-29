package com.example.appmedica.db
import com.example.appmedica.model.Paciente
class DBClass {
    val ListaDePacientes = mutableListOf<Paciente>()
    fun agregar(newPaciente: Paciente)
    {
        ListaDePacientes.add(newPaciente)
    }
    fun getPaciente(nombre: String): Paciente?
    {
        return ListaDePacientes.find { it.nombre == "Carlos" }
    }
    fun getPacientes(): List<Paciente>
    {
        return ListaDePacientes
    }
}