package com.example.appmedica.repository

import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.data.local.CitaEntity
import com.example.appmedica.model.Cita
import com.example.appmedica.model.EstadoCita
import com.example.appmedica.model.FormularioCita
import com.example.appmedica.model.RolDemo
import com.example.appmedica.model.validarCita
import java.util.UUID
import kotlinx.coroutines.flow.map

// Reglas del prototipo local. No sustituyen autorización en un servidor.
class CitaRepository(database: AppDatabase) {
    private val dao = database.citaDao()
    val citas = dao.observarTodas().map { lista -> lista.map { it.toDominio() } }
    fun observarCita(id: String) = dao.observarPorId(id).map { it?.toDominio() }
    fun observarAgenda(estado: EstadoCita?, pacienteId: String?) =
        dao.observarFiltradas(estado, pacienteId).map { lista -> lista.map { it.toDominio() } }

    fun observarProxima() = dao.observarProxima().map { it?.toDominio() }

    suspend fun guardar(formulario: FormularioCita, rol: RolDemo) {
        require(rol == RolDemo.MEDICO) { "Solo el médico demo puede guardar citas." }
        validarCita(formulario)?.let { throw IllegalArgumentException(it) }
        val fecha = formulario.fecha.trim()
        val hora = formulario.hora.trim()
        val motivo = formulario.motivo.trim()
        if (formulario.id == null) {
            dao.insertar(CitaEntity(UUID.randomUUID().toString(), formulario.pacienteId,
                fecha, hora, motivo, EstadoCita.PENDIENTE))
        } else {
            check(dao.editarPendiente(formulario.id, formulario.pacienteId, fecha, hora, motivo) == 1) {
                "La cita ya no está pendiente o fue eliminada."
            }
        }
    }

    suspend fun responder(id: String, pacienteId: String, estado: EstadoCita, rol: RolDemo) {
        require(rol == RolDemo.PACIENTE) { "Selecciona el rol paciente demo para responder." }
        require(estado == EstadoCita.ACEPTADA || estado == EstadoCita.RECHAZADA) { "Respuesta inválida." }
        check(dao.responderPendiente(id, pacienteId, estado) == 1) {
            "La cita no pertenece a este paciente, ya fue respondida o fue eliminada."
        }
    }

    suspend fun eliminar(id: String, rol: RolDemo) {
        require(rol == RolDemo.MEDICO) { "Solo el médico demo puede eliminar citas." }
        check(dao.eliminar(id) == 1) { "La cita ya fue eliminada." }
    }
}

private fun CitaEntity.toDominio() = Cita(id, pacienteId, fecha, hora, motivo, estado)
