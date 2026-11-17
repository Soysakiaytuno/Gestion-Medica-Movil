
package com.example.appmedica.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.model.SesionUsuario
import com.example.appmedica.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoAutenticacion {
    data object SinSesion : EstadoAutenticacion
    data object Cargando : EstadoAutenticacion

    data class Autenticado(
        val sesion: SesionUsuario
    ) : EstadoAutenticacion

    data class Error(
        val mensaje: String
    ) : EstadoAutenticacion
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _estado =
        MutableStateFlow<EstadoAutenticacion>(
            EstadoAutenticacion.SinSesion
        )

    val estado: StateFlow<EstadoAutenticacion> =
        _estado.asStateFlow()

    fun iniciarSesion(
        correo: String,
        contrasena: String
    ) {
        if (_estado.value == EstadoAutenticacion.Cargando) return

        viewModelScope.launch {
            _estado.value = EstadoAutenticacion.Cargando

            try {
                val sesion = repository.iniciarSesion(
                    correo = correo,
                    contrasena = contrasena
                )

                _estado.value =
                    EstadoAutenticacion.Autenticado(sesion)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = EstadoAutenticacion.Error(
                    mensaje = e.message ?: "No se pudo iniciar sesión"
                )
            }
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _estado.value = EstadoAutenticacion.SinSesion
    }
}
