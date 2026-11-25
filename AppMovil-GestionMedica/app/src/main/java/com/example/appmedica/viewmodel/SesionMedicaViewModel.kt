package com.example.appmedica.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.data.remote.SupabaseClient
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.repository.AlmacenMedico
import com.example.appmedica.repository.CitaRepository
import com.example.appmedica.repository.ErrorOperacion
import com.example.appmedica.repository.PacienteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoSincronizacion(val cargando: Boolean = false, val error: String = "")

class SesionMedicaViewModel(context: Context, perfil: PerfilUsuario) : ViewModel() {
    private val database = AppDatabase.paraSesion(context, "${perfil.uid}|${perfil.rol}|${perfil.pacienteId}")
    private val api by lazy { SupabaseClient.crear() }
    private val almacen = AlmacenMedico(database, { api }, { SupabaseClient.token(perfil.uid) })
    val pacientes = PacienteRepository(almacen)
    val citas = CitaRepository(almacen)
    private val mutableEstado = MutableStateFlow(EstadoSincronizacion())
    val estado = mutableEstado.asStateFlow()
    private var solicitud: Job? = null

    fun actualizar() {
        if (solicitud?.isActive == true) return
        solicitud = viewModelScope.launch {
            mutableEstado.value = EstadoSincronizacion(cargando = true)
            try {
                almacen.actualizar()
                mutableEstado.value = EstadoSincronizacion()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                val mensaje = if (e is ErrorOperacion) e.message else
                    "No se pudieron actualizar los datos. Desliza hacia abajo para volver a intentarlo."
                mutableEstado.value = EstadoSincronizacion(error =
                    "$mensaje Se muestran los últimos datos guardados en este teléfono.")
            }
        }
    }

    override fun onCleared() { database.close() }
}
