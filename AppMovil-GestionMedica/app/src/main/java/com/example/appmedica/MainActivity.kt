
package com.example.appmedica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

                                    BarraSesion(
                                        titulo = "Área médica",
                                        onCerrarSesion = authViewModel::cerrarSesion
                                    )

                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AppNavigation(pacientes, citas)
                                    }
                                }
                            }

                            RolUsuario.PACIENTE -> {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {

                                    BarraSesion(
                                        titulo = "Área del paciente",
                                        onCerrarSesion = authViewModel::cerrarSesion
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "Bienvenido, ${estado.sesion.perfil.nombre}",
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            Text(
                                                text = "Acceso de paciente: pantalla en preparación",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
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

@Composable
private fun BarraSesion(
    titulo: String,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar sesión")
            }
        }

        HorizontalDivider()
    }
}
