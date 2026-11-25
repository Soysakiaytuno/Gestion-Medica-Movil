package com.example.appmedica.repository

import com.example.appmedica.data.remote.*
import com.example.appmedica.data.local.CitaEntity
import com.example.appmedica.model.Cita
import com.example.appmedica.model.EstadoCita
import com.example.appmedica.model.FormularioCita
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.model.validarCita
import kotlinx.coroutines.flow.map

// Las validaciones locales ayudan a la UI; el servidor comprueba los permisos y el estado.
class CitaRepository(private val almacen: AlmacenMedico) {
    private val dao = almacen.database.citaDao()
    fun observarCita(id: String) = dao.observarPorId(id).map { it?.toDominio() }
    fun observarAgenda(estado: EstadoCita?, pacienteId: String?) =
        dao.observarFiltradas(estado, pacienteId).map { lista -> lista.map { it.toDominio() } }

    fun observarProxima(pacienteId: String? = null) = dao.observarProxima(pacienteId).map { it?.toDominio() }

    suspend fun guardar(formulario: FormularioCita, rol: RolUsuario) {
        require(rol == RolUsuario.MEDICO) { "Solo el médico puede guardar citas." }
        validarCita(formulario)?.let { throw IllegalArgumentException(it) }
        val fecha = formulario.fecha.trim()
        val hora = formulario.hora.trim()
        val motivo = formulario.motivo.trim()
        almacen.ejecutar {
            guardarCita(it, GuardarCitaPeticion(formulario.id, formulario.pacienteId, fecha, hora, motivo))
        }
    }

    suspend fun responder(id: String, estado: EstadoCita, rol: RolUsuario) {
        require(rol == RolUsuario.PACIENTE) { "Solo el paciente puede responder sus citas." }
        require(estado == EstadoCita.ACEPTADA || estado == EstadoCita.RECHAZADA) { "Respuesta inválida." }
        // El pacienteId real se obtiene del perfil del servidor, nunca del cuerpo de la petición.
        almacen.ejecutar { responderCita(it, ResponderCitaPeticion(id, estado)) }
    }

    suspend fun eliminar(id: String, rol: RolUsuario) {
        require(rol == RolUsuario.MEDICO) { "Solo el médico puede eliminar citas." }
        almacen.ejecutar { eliminarCita(it, EliminarPeticion(id)) }
    }
}

private fun CitaEntity.toDominio() = Cita(id, pacienteId, fecha, hora, motivo, estado, notificado)
