package com.example.appmedica.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmedica.repository.PacienteRepository
import com.example.appmedica.ui.screens.BuscarPacienteScreen
import com.example.appmedica.ui.screens.HistorialMedicoScreen
import com.example.appmedica.ui.screens.InicioScreen
import com.example.appmedica.ui.screens.RegistrarPacienteScreen
import com.example.appmedica.viewmodel.BuscarPacienteViewModel
import com.example.appmedica.viewmodel.HistorialViewModel
import com.example.appmedica.viewmodel.RegistroViewModel

private const val INICIO = "inicio"
private const val BUSCAR_PACIENTE = "buscar_paciente"
private const val REGISTRAR_PACIENTE = "registrar_paciente"
private const val HISTORIAL_MEDICO = "historial_medico"
private const val PACIENTE_ID = "pacienteId"

@Composable
fun AppNavigation(repository: PacienteRepository) {
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
            val busqueda: BuscarPacienteViewModel = viewModel { BuscarPacienteViewModel(repository) }
            val query by busqueda.query.collectAsStateWithLifecycle()
            val pacientes by busqueda.pacientes.collectAsStateWithLifecycle()
            InicioScreen(
                query = query,
                pacientes = pacientes,
                onQueryChange = busqueda::onQueryChange,
                irBuscarPaciente = {
                    navController.navigate(BUSCAR_PACIENTE) { launchSingleTop = true }
                },
                irAgregarPaciente = registrar,
                irDetallesPaciente = abrirHistorial
            )
        }

        composable(BUSCAR_PACIENTE) {
            val busqueda: BuscarPacienteViewModel = viewModel { BuscarPacienteViewModel(repository) }
            val query by busqueda.query.collectAsStateWithLifecycle()
            val pacientes by busqueda.pacientes.collectAsStateWithLifecycle()
            BuscarPacienteScreen(
                query = query,
                pacientes = pacientes,
                onQueryChange = busqueda::onQueryChange,
                volver = volver,
                irAgregar = registrar,
                irDetalles = abrirHistorial
            )
        }

        composable(REGISTRAR_PACIENTE) { entrada ->
            val registro: RegistroViewModel = viewModel { RegistroViewModel(repository) }
            val estado by registro.estado.collectAsStateWithLifecycle()
            RegistrarPacienteScreen(
                formulario = estado.formulario,
                mensajeError = estado.mensajeError,
                guardando = estado.guardando,
                onFormularioChange = registro::actualizarFormulario,
                onGuardar = {
                    registro.guardar {
                        if (navController.currentBackStackEntry == entrada) volver()
                    }
                },
                onVolver = volver
            )
        }

        composable(
            route = "$HISTORIAL_MEDICO/{$PACIENTE_ID}",
            arguments = listOf(navArgument(PACIENTE_ID) { type = NavType.StringType })
        ) { entrada ->
            val historial: HistorialViewModel = viewModel {
                HistorialViewModel(repository, entrada.arguments?.getString(PACIENTE_ID))
            }
            val paciente by historial.paciente.collectAsStateWithLifecycle()
            HistorialMedicoScreen(paciente = paciente, onVolver = volver)
        }
    }
}
