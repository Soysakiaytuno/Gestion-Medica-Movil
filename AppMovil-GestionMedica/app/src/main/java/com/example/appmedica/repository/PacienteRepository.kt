package com.example.appmedica.repository

import com.example.appmedica.data.local.PacienteEntity
import com.example.appmedica.data.remote.GuardarPacientePeticion
import com.example.appmedica.data.remote.PacienteDto
import com.example.appmedica.model.Paciente
import java.time.LocalDate
import kotlinx.coroutines.flow.map

enum class ResultadoRegistroPaciente { GUARDADO, CI_DUPLICADO, EMAIL_DUPLICADO }

class PacienteRepository(private val almacen: AlmacenMedico) {
    private val dao = almacen.database.pacienteDao()
    val pacientes = dao.observarTodos().map { lista -> lista.map { it.toDominio() } }
    fun observarPaciente(id: String) = dao.observarPorId(id).map { it?.toDominio() }

    suspend fun registrar(paciente: Paciente): ResultadoRegistroPaciente {
        // Estas consultas dan respuesta inmediata; las restricciones únicas del servidor
        // vuelven a comprobar los duplicados entre dispositivos.
        if (dao.existeCi(paciente.ci)) return ResultadoRegistroPaciente.CI_DUPLICADO
        if (dao.existeEmail(paciente.email)) return ResultadoRegistroPaciente.EMAIL_DUPLICADO
        guardar(paciente)
        return ResultadoRegistroPaciente.GUARDADO
    }

    private suspend fun guardar(paciente: Paciente) {
        almacen.ejecutar { guardarPaciente(it, GuardarPacientePeticion(paciente.toDto())) }
    }


}

private fun PacienteEntity.toDominio() = Paciente(id, nombre, apellidoPaterno, apellidoMaterno,
    ci, email, celular, diagnosticos, LocalDate.parse(fechaNacimiento))

private fun Paciente.toDto() = PacienteDto(id, nombre, apellidoPaterno, apellidoMaterno, ci,
    email, celular, diagnosticos, fechaNacimiento.toString())
