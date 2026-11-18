package com.example.appmedica.viewmodel

import android.database.SQLException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.model.Cita
import com.example.appmedica.model.EstadoCita
import com.example.appmedica.model.FormularioCita
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.repository.CitaRepository
import com.example.appmedica.repository.PacienteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgendaDatos(
    val pacientes: List<Paciente> = emptyList(),
    val citas: List<Cita> = emptyList(),
    val cargando: Boolean = true,
    val error: String = "",
    val consulta: ConsultaAgenda? = null
)

data class ConsultaAgenda(val estado: EstadoCita?, val pacienteId: String?)

data class CitasEstado(
    val rol: RolUsuario,
    val pacienteId: String? = null,
    val formulario: FormularioCita = FormularioCita(),
    val ocupado: Boolean = false,
    val error: String = "",
    val filtro: EstadoCita? = null
) {
    val consulta: ConsultaAgenda
        get() = ConsultaAgenda(filtro, pacienteId?.takeIf { rol == RolUsuario.PACIENTE && it.isNotBlank() })
}

data class DetalleCitaDatos(
    val cita: Cita? = null,
    val cargando: Boolean = true,
    val error: String = ""
)

data class ProximaCita(
    val citaId: String,
    val paciente: String,
    val motivo: String,
    val fecha: String,
    val hora: String
)

/** Si no hay cita, consulta es null y se muestra el mensaje. */
data class ResumenProxima(
    val cita: ProximaCita? = null,
    val mensaje: String = "Cargando agenda…"
)

class CitasViewModel(
    private val repository: CitaRepository,
    pacientes: PacienteRepository,
    perfil: PerfilUsuario
) : ViewModel() {
    // El rol y el paciente vienen de la sesión de Firebase y no cambian durante la sesión
    private val esPaciente = perfil.rol == RolUsuario.PACIENTE
    private val pacienteIdSeguro = perfil.pacienteId?.takeIf { it.isNotBlank() }
    private val mutableEstado = MutableStateFlow(CitasEstado(rol = perfil.rol, pacienteId = pacienteIdSeguro))
    val estado = mutableEstado.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val datos = estado.map { it.consulta }.distinctUntilChanged()
        .flatMapLatest { consulta ->
            if (esPaciente && consulta.pacienteId.isNullOrBlank()) {
                flowOf(AgendaDatos(cargando = false, consulta = consulta))
            } else {
                val flujoPacientes = if (esPaciente) {
                    pacientes.observarPaciente(consulta.pacienteId!!).map(::listOfNotNull)
                } else {
                    pacientes.pacientes
                }
                combine(
                    flujoPacientes,
                    repository.observarAgenda(consulta.estado, consulta.pacienteId)
                ) { listaPacientes, listaCitas ->
                    AgendaDatos(listaPacientes, listaCitas, cargando = false, consulta = consulta)
                }.onStart {
                    emit(AgendaDatos(consulta = consulta))
                }.catch { error ->
                    if (error is CancellationException) throw error
                    emit(AgendaDatos(cargando = false, error = "No se pudo cargar la agenda.", consulta = consulta))
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgendaDatos())

    private val mensajeSinProximaCita =
        if (esPaciente) "No tienes próximas citas." else "No hay próximas citas."

    val resumenProxima = if (esPaciente && pacienteIdSeguro == null) {
        flowOf(ResumenProxima(mensaje = mensajeSinProximaCita))
    } else {
        val flujoProxima = repository.observarProxima(if (esPaciente) pacienteIdSeguro else null)
        val flujoPacientes = if (esPaciente) {
            pacientes.observarPaciente(pacienteIdSeguro!!).map(::listOfNotNull)
        } else {
            pacientes.pacientes
        }
        combine(flujoProxima, flujoPacientes) { cita, lista ->
            if (cita == null) {
                ResumenProxima(mensaje = mensajeSinProximaCita)
            } else {
                val paciente = lista.firstOrNull { it.id == cita.pacienteId }
                val nombrePaciente = paciente?.let {
                    listOf(it.apellidoPaterno.trim(), it.nombre.trim())
                        .filter { parte -> parte.isNotEmpty() }
                        .joinToString(" ")
                        .ifEmpty { "Paciente" }
                } ?: "Paciente"
                ResumenProxima(
                    ProximaCita(
                        citaId = cita.id,
                        paciente = nombrePaciente,
                        motivo = cita.motivo,
                        fecha = cita.fecha,
                        hora = cita.hora
                    )
                )
            }
        }.catch { error ->
            if (error is CancellationException) throw error
            emit(ResumenProxima(mensaje = "No se pudo cargar la próxima cita."))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumenProxima())

    fun seleccionarFiltro(filtro: EstadoCita?) {
        mutableEstado.update { it.copy(filtro = filtro) }
    }

    fun observarDetalle(id: String) = repository.observarCita(id)
        .map { DetalleCitaDatos(cita = it, cargando = false) }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(DetalleCitaDatos(cargando = false, error = "No se pudo cargar la cita."))
        }

    fun prepararFormulario(cita: Cita? = null) {
        mutableEstado.update { it.copy(error = "", formulario = cita?.let { c ->
            FormularioCita(c.id, c.pacienteId, c.fecha, c.hora, c.motivo)
        } ?: FormularioCita()) }
    }

    fun cambiarFormulario(formulario: FormularioCita) {
        if (!estado.value.ocupado) mutableEstado.update { it.copy(formulario = formulario, error = "") }
    }

    fun limpiarError() { mutableEstado.update { it.copy(error = "") } }

    fun guardar(alGuardar: () -> Unit) {
        val actual = estado.value
        ejecutar(alGuardar) { repository.guardar(actual.formulario, actual.rol) }
    }

    fun responder(id: String, respuesta: EstadoCita) {
        val actual = estado.value
        val pacienteId = actual.pacienteId ?: return
        ejecutar { repository.responder(id, pacienteId, respuesta, actual.rol) }
    }

    fun eliminar(id: String, alEliminar: () -> Unit) {
        val rol = estado.value.rol
        ejecutar(alEliminar) { repository.eliminar(id, rol) }
    }

    private fun ejecutar(alTerminar: () -> Unit = {}, accion: suspend () -> Unit) {
        if (estado.value.ocupado) return
        mutableEstado.update { it.copy(ocupado = true, error = "") }
        viewModelScope.launch {
            try {
                accion()
                alTerminar()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: SQLException) {
                mutableEstado.update { it.copy(error = "No se pudo guardar el cambio. Comprueba que el paciente siga registrado e intenta nuevamente.") }
            } catch (error: IllegalArgumentException) {
                mutableEstado.update { it.copy(error = error.message ?: "Datos inválidos.") }
            } catch (error: IllegalStateException) {
                mutableEstado.update { it.copy(error = error.message ?: "La cita cambió. Vuelve a abrir su detalle.") }
            } finally {
                mutableEstado.update { it.copy(ocupado = false) }
            }
        }
    }
}
