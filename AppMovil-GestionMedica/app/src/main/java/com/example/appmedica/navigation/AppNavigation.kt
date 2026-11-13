package com.example.appmedica.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmedica.ui.screens.BuscarPacienteScreen
import com.example.appmedica.ui.screens.HistorialMedicoScreen
import com.example.appmedica.ui.screens.InicioScreen
import com.example.appmedica.ui.screens.RegistrarPacienteScreen
import com.example.appmedica.viewmodel.BuscarPacienteViewModel
import com.example.appmedica.viewmodel.HistorialViewModel
import com.example.appmedica.viewmodel.HistorialViewModel.Companion.PACIENTE_ID
import com.example.appmedica.viewmodel.RegistroViewModel

private const val INICIO = "inicio"
private const val BUSCAR_PACIENTE = "buscar_paciente"
private const val REGISTRAR_PACIENTE = "registrar_paciente"
private const val HISTORIAL_MEDICO = "historial_medico"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val volver: () -> Unit = { navController.popBackStack() }
    val registrar: () -> Unit = {
        navController.navigate(REGISTRAR_PACIENTE) { launchSingleTop = true }
    }
    val abrirHistorial: (String) -> Unit = { pacienteId ->
        navController.navigate("$HISTORIAL_MEDICO/${Uri.encode(pacienteId)}") {
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = INICIO) {
        composable(INICIO) {
            val busqueda: BuscarPacienteViewModel = viewModel()
            InicioScreen(
                query = busqueda.query,
                pacientes = busqueda.pacientesFiltrados(),
                onQueryChange = busqueda::onQueryChange,
                irBuscarPaciente = {
                    navController.navigate(BUSCAR_PACIENTE) { launchSingleTop = true }
                },
                irAgregarPaciente = registrar,
                irDetallesPaciente = abrirHistorial
            )
        }

        composable(BUSCAR_PACIENTE) {
            val busqueda: BuscarPacienteViewModel = viewModel()
            BuscarPacienteScreen(
                query = busqueda.query,
                pacientes = busqueda.pacientesFiltrados(),
                onQueryChange = busqueda::onQueryChange,
                volver = volver,
                irAgregar = registrar,
                irDetalles = abrirHistorial
            )
        }

        composable(REGISTRAR_PACIENTE) {
            val registro: RegistroViewModel = viewModel()
            RegistrarPacienteScreen(
                formulario = registro.formulario,
                mensajeError = registro.mensajeError,
                onFormularioChange = registro::actualizarFormulario,
                onGuardar = { if (registro.guardar()) volver() },
                onVolver = volver
            )
        }

        composable(
            route = "$HISTORIAL_MEDICO/{$PACIENTE_ID}",
            arguments = listOf(navArgument(PACIENTE_ID) { type = NavType.StringType })
        ) {
            val historial: HistorialViewModel = viewModel()
            HistorialMedicoScreen(paciente = historial.paciente, onVolver = volver)
        }
    }
}
