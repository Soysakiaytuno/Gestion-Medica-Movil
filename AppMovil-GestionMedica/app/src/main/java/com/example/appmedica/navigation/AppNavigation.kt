package com.example.appmedica.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.appmedica.repository.CitaRepository
import com.example.appmedica.viewmodel.CitasViewModel
import com.example.appmedica.viewmodel.DetalleCitaDatos
import com.example.appmedica.ui.screens.AgendaCitasScreen
import com.example.appmedica.ui.screens.FormularioCitaScreen
import com.example.appmedica.ui.screens.DetalleCitaScreen

private const val INICIO = "inicio"
private const val BUSCAR_PACIENTE = "buscar_paciente"
private const val REGISTRAR_PACIENTE = "registrar_paciente"
private const val HISTORIAL_MEDICO = "historial_medico"
private const val PACIENTE_ID = "pacienteId"

@Composable
fun AppNavigation(repository: PacienteRepository, citasRepository: CitaRepository) {
    val navController = rememberNavController()
    val agenda: CitasViewModel = viewModel { CitasViewModel(citasRepository, repository) }
    val datosAgenda by agenda.datos.collectAsStateWithLifecycle()
    val estadoAgenda by agenda.estado.collectAsStateWithLifecycle()
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
            val resumenProxima by agenda.resumenProxima.collectAsStateWithLifecycle()
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
                irDetallesPaciente = abrirHistorial,
                irAgenda = { navController.navigate("citas") { launchSingleTop = true } },
                resumenCita = resumenProxima
            )
        }

        composable("citas") {
            AgendaCitasScreen(datosAgenda, estadoAgenda, agenda::seleccionarRol, agenda::seleccionarPaciente,
                onFiltro = agenda::seleccionarFiltro,
                onNueva = {
                    agenda.prepararFormulario()
                    navController.navigate("formulario_cita") { launchSingleTop = true }
                },
                onDetalle = { id ->
                    agenda.limpiarError()
                    navController.navigate("cita/${Uri.encode(id)}") { launchSingleTop = true }
                }, onVolver = volver)
        }
        composable("formulario_cita") { entrada ->
            FormularioCitaScreen(datosAgenda, estadoAgenda, agenda::cambiarFormulario,
                onGuardar = { agenda.guardar { if (navController.currentBackStackEntry == entrada) volver() } },
                onVolver = volver)
        }
        composable("cita/{citaId}", arguments = listOf(navArgument("citaId") { type = NavType.StringType })) { entrada ->
            val id = entrada.arguments?.getString("citaId").orEmpty()
            val detalle by remember(agenda, id) { agenda.observarDetalle(id) }
                .collectAsStateWithLifecycle(initialValue = DetalleCitaDatos())
            DetalleCitaScreen(id, detalle, datosAgenda.pacientes, estadoAgenda,
                onEditar = { cita ->
                    agenda.prepararFormulario(cita)
                    navController.navigate("formulario_cita") { launchSingleTop = true }
                },
                onResponder = { agenda.responder(id, it) },
                onEliminar = { agenda.eliminar(id) { if (navController.currentBackStackEntry == entrada) volver() } },
                onVolver = volver)
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
