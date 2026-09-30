package com.example.appmedica.db

import android.annotation.SuppressLint
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.Paciente
import java.time.LocalDate

object DBClass {
    @SuppressLint("NewApi")
    private val listaDePacientes = mutableListOf(
        Paciente(
            id = "paciente-josue-01",
            nombre = "Josue",
            apellidoPaterno = "Balbontin",
            apellidoMaterno = "Perez",
            ci = "8493021",
            email = "josue.balbontin@email.com",
            celular = "70012345",
            diagnosticos = listOf(
                Diagnostico(
                    fecha = "04/10/2026",
                    titulo = "Asma",
                    descripcion = "El paciente presenta Tos insistente\n\nla enfermedad es tos\n\nse le receta tal cosa\n\nporque desinflama el pulmon"
                ),
                Diagnostico(
                    fecha = "18/08/2026",
                    titulo = "Bronquitis Aguda",
                    descripcion = "El paciente acude por dificultad respiratoria leve\n\nse detecta inflamación bronquial\n\nse receta inhalador cada 8 horas"
                )
            ),
            fechaNacimiento = LocalDate.of(1999, 5, 14)
        )
    )

    fun agregar(newPaciente: Paciente) {
        listaDePacientes.add(newPaciente)
    }

    fun getPaciente(nombre: String): Paciente? {
        return listaDePacientes.find {
            it.nombre.equals(nombre, ignoreCase = true) || it.id == nombre
        }
    }

    fun getPacientePorId(id: String): Paciente? {
        return listaDePacientes.find { it.id == id }
    }

    fun getPacientes(): List<Paciente> {
        return listaDePacientes.toList()
    }
}