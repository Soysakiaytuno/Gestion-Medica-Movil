package com.example.appmedica.viewmodel

import android.database.SQLException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.model.Cita
import com.example.appmedica.model.EstadoCita
import com.example.appmedica.model.FormularioCita
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.RolDemo
import com.example.appmedica.repository.CitaRepository
import com.example.appmedica.repository.PacienteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgendaDatos(
    val pacientes: List<Paciente> = emptyList(),
    val citas: List<Cita> = emptyList(),
    val cargando: Boolean = true,
    val error: String = ""
)

data class CitasEstado(
    val rol: RolDemo = RolDemo.MEDICO,
    val pacienteDemoId: String = "",
    val formulario: FormularioCita = FormularioCita(),
    val ocupado: Boolean = false,
    val error: String = ""
)

class CitasViewModel(
    private val repository: CitaRepository,
    pacientes: PacienteRepository
) : ViewModel() {
    val datos = combine(pacientes.pacientes, repository.citas) { listaPacientes, listaCitas ->
        AgendaDatos(listaPacientes, listaCitas, cargando = false)
    }.catch { error ->
        if (error is CancellationException) throw error
        emit(AgendaDatos(cargando = false, error = "No se pudo cargar la agenda. Vuelve a abrir la aplicación."))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgendaDatos())

    private val mutableEstado = MutableStateFlow(CitasEstado())
    val estado = mutableEstado.asStateFlow()

    fun seleccionarRol(rol: RolDemo) {
        if (!estado.value.ocupado) mutableEstado.update { it.copy(rol = rol, error = "") }
    }

    fun seleccionarPaciente(id: String) {
        if (!estado.value.ocupado) mutableEstado.update { it.copy(pacienteDemoId = id, error = "") }
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
        ejecutar { repository.responder(id, actual.pacienteDemoId, respuesta, actual.rol) }
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
