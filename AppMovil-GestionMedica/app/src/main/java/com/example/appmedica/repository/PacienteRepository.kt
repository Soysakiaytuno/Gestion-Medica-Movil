package com.example.appmedica.repository

import androidx.room.withTransaction
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.data.local.PacienteEntity
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.model.RolUsuario
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

    suspend fun asegurarPacienteParaPerfil(perfil: PerfilUsuario) {
        if (perfil.rol != RolUsuario.PACIENTE) return
        val pacienteId = perfil.pacienteId?.takeIf { it.isNotBlank() } ?: return
        database.withTransaction {
            if (dao.existeId(pacienteId)) return@withTransaction
            dao.insertar(
                PacienteEntity(
                    id = pacienteId,
                    nombre = perfil.nombre.trim().ifBlank { "Paciente" },
                    apellidoPaterno = "",
                    apellidoMaterno = "",
                    ci = generarCiDisponible(pacienteId),
                    email = generarEmailDisponible(pacienteId, perfil.correo),
                    celular = "",
                    diagnosticos = emptyList(),
                    fechaNacimiento = LocalDate.of(2000, 1, 1).toString()
                )
            )
        }
    }

    private suspend fun generarCiDisponible(pacienteId: String): String {
        val base = "SIN-CI-$pacienteId"
        if (!dao.existeCi(base)) return base
        var sufijo = 1
        while (dao.existeCi("$base-$sufijo")) {
            sufijo++
        }
        return "$base-$sufijo"
    }

    private suspend fun generarEmailDisponible(pacienteId: String, correo: String): String {
        val correoLimpio = correo.trim()
        if (correoLimpio.isNotBlank() && !dao.existeEmail(correoLimpio)) {
            return correoLimpio
        }
        val base = "$pacienteId@paciente.local"
        if (!dao.existeEmail(base)) return base
        var sufijo = 1
        while (dao.existeEmail("$pacienteId-$sufijo@paciente.local")) {
            sufijo++
        }
        return "$pacienteId-$sufijo@paciente.local"
    }
}

private fun PacienteEntity.toDominio() = Paciente(
    id, nombre, apellidoPaterno, apellidoMaterno, ci, email, celular, diagnosticos,
    LocalDate.parse(fechaNacimiento)
)

private fun Paciente.toEntity() = PacienteEntity(
    id, nombre, apellidoPaterno, apellidoMaterno, ci, email, celular, diagnosticos,
    fechaNacimiento.toString()
)
