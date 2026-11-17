
package com.example.appmedica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.navigation.AppNavigation
import com.example.appmedica.repository.CitaRepository
import com.example.appmedica.repository.PacienteRepository
import com.example.appmedica.ui.screens.LoginScreen
import com.example.appmedica.viewmodel.AuthViewModel
import com.example.appmedica.viewmodel.EstadoAutenticacion

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.obtener(applicationContext)
        val pacientes = PacienteRepository(database)
        val citas = CitaRepository(database)

        setContent {
            MaterialTheme {

                val authViewModel: AuthViewModel = viewModel {
                    AuthViewModel()
                }

                val estadoAuth by authViewModel.estado
                    .collectAsStateWithLifecycle()

                when (val estado = estadoAuth) {

                    is EstadoAutenticacion.Autenticado -> {

                        when (estado.sesion.perfil.rol) {

                            RolUsuario.MEDICO -> {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = authViewModel::cerrarSesion
                                        ) {
                                            Text("Cerrar sesión")
                                        }
                                    }

                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AppNavigation(pacientes, citas)
                                    }
                                }
                            }

                            RolUsuario.PACIENTE -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Bienvenido, ${estado.sesion.perfil.nombre}"
                                    )

                                    Text(
                                        "Acceso de paciente: pantalla en preparación"
                                    )

                                    Button(
                                        onClick = authViewModel::cerrarSesion
                                    ) {
                                        Text("Cerrar sesión")
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        LoginScreen(
                            estado = estado,
                            onIniciarSesion = authViewModel::iniciarSesion
                        )
                    }
                }
            }
        }
    }
}
