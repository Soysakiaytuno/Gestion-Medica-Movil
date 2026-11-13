package com.example.appmedica.repository

import androidx.room.withTransaction
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.data.local.PacienteEntity
import com.example.appmedica.model.Paciente
import java.time.LocalDate
import kotlinx.coroutines.flow.map

enum class ResultadoRegistroPaciente {
    GUARDADO, CI_DUPLICADO, EMAIL_DUPLICADO
}

class PacienteRepository(private val database: AppDatabase) {
    private val dao = database.pacienteDao()

    val pacientes = dao.observarTodos().map { pacientes -> pacientes.map { it.toDominio() } }

    fun observarPaciente(id: String) = dao.observarPorId(id).map { it?.toDominio() }

    suspend fun registrar(paciente: Paciente): ResultadoRegistroPaciente = database.withTransaction {
        when {
            dao.existeCi(paciente.ci) -> ResultadoRegistroPaciente.CI_DUPLICADO
            dao.existeEmail(paciente.email) -> ResultadoRegistroPaciente.EMAIL_DUPLICADO
            else -> {
                dao.insertar(paciente.toEntity())
                ResultadoRegistroPaciente.GUARDADO
            }
        }
    }

    suspend fun actualizar(paciente: Paciente): Boolean = dao.actualizar(paciente.toEntity()) > 0

    suspend fun eliminar(id: String): Boolean = dao.eliminar(id) > 0
}

private fun PacienteEntity.toDominio() = Paciente(
    id, nombre, apellidoPaterno, apellidoMaterno, ci, email, celular, diagnosticos,
    LocalDate.parse(fechaNacimiento)
)

private fun Paciente.toEntity() = PacienteEntity(
    id, nombre, apellidoPaterno, apellidoMaterno, ci, email, celular, diagnosticos,
    fechaNacimiento.toString()
)
