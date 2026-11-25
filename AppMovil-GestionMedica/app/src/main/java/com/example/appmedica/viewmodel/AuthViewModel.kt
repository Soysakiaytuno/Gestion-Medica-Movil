
package com.example.appmedica.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.model.SesionUsuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.example.appmedica.repository.ErrorOperacion
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
                    mensaje = when (e) {
                        is FirebaseNetworkException -> "No hay conexión. Revisa tu internet y vuelve a intentarlo."
                        is FirebaseTooManyRequestsException -> "Hubo demasiados intentos. Espera un momento y vuelve a intentarlo."
                        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
                            "Revisa tu correo y contraseña e inténtalo de nuevo."
                        is ErrorOperacion -> e.message ?: "Revisa los datos ingresados."
                        else -> "No se pudo iniciar sesión. Vuelve a intentarlo; si el problema continúa, contacta al responsable."
                    }
                )
            }
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _estado.value = EstadoAutenticacion.SinSesion
    }
}
