package com.example.appmedica.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmedica.data.remote.RetrofitClient
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.repository.CatalogoMedicamentosRepository
import com.example.appmedica.repository.PacienteRepository
import com.example.appmedica.ui.components.ContenidoActualizable
import com.example.appmedica.viewmodel.CatalogoMedicamentosEstado
import com.example.appmedica.ui.components.BotonVolver
import com.example.appmedica.ui.screens.CatalogoMedicamentosScreen
import com.example.appmedica.ui.screens.HistorialMedicoScreen
import com.example.appmedica.ui.screens.InicioPacienteScreen
import com.example.appmedica.ui.screens.InicioScreen
import com.example.appmedica.ui.screens.RegistrarPacienteScreen
import com.example.appmedica.ui.screens.TopBarHistorial
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco
import com.example.appmedica.ui.theme.TextoOscuro
import com.example.appmedica.viewmodel.BuscarPacienteViewModel
import com.example.appmedica.viewmodel.CatalogoMedicamentosViewModel
import com.example.appmedica.viewmodel.HistorialViewModel
import com.example.appmedica.viewmodel.RegistroViewModel
import com.example.appmedica.repository.CitaRepository
import com.example.appmedica.viewmodel.CitasViewModel
import com.example.appmedica.viewmodel.DetalleCitaDatos
import com.example.appmedica.ui.screens.AgendaCitasScreen
import com.example.appmedica.ui.screens.FormularioCitaScreen
import com.example.appmedica.ui.screens.DetalleCitaScreen
import kotlinx.coroutines.flow.flowOf

private const val INICIO = "inicio"
private const val INICIO_PACIENTE = "inicio_paciente"
private const val REGISTRAR_PACIENTE = "registrar_paciente"
private const val HISTORIAL_MEDICO = "historial_medico"
private const val PACIENTE_ID = "pacienteId"
private const val CATALOGO_MEDICAMENTOS = "catalogo_medicamentos"

@Composable
fun AppNavigation(repository: PacienteRepository, citasRepository: CitaRepository, perfil: PerfilUsuario,
    actualizando: Boolean = false, onActualizar: () -> Unit = {}) {
    val navController = rememberNavController()
    val esPaciente = perfil.rol == RolUsuario.PACIENTE
    val esMedico = perfil.rol == RolUsuario.MEDICO
    val startDestination = if (esPaciente) INICIO_PACIENTE else INICIO
    // La clave incluye rol y pacienteId para aislar la agenda ante cambios de perfil
    val agenda: CitasViewModel = viewModel(
        key = "citas-${perfil.uid}-${perfil.rol.name}-${perfil.pacienteId.orEmpty()}"
    ) {
        CitasViewModel(citasRepository, repository, perfil)
    }
    val datosAgenda by agenda.datos.collectAsStateWithLifecycle()
    val estadoAgenda by agenda.estado.collectAsStateWithLifecycle()
    val volver: () -> Unit = { navController.popBackStack() }
    val redirigirAlInicio: () -> Unit = {
        val destino = if (esPaciente) INICIO_PACIENTE else INICIO
        navController.navigate(destino) {
            popUpTo(destino) { inclusive = false }
            launchSingleTop = true
        }
    }
    val registrar: () -> Unit = {
        navController.navigate(REGISTRAR_PACIENTE) { launchSingleTop = true }
    }
    val abrirHistorial: (String) -> Unit = { pacienteId ->
        navController.navigate("$HISTORIAL_MEDICO/${Uri.encode(pacienteId)}") {
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(INICIO_PACIENTE) {
            if (!esPaciente) {
                RutaRestringida(
                    mensaje = "Esta sección es exclusiva para pacientes.",
                    alRedirigir = redirigirAlInicio,
                    onVolver = volver
                )
            } else {
                val resumenProxima by agenda.resumenProxima.collectAsStateWithLifecycle()
                val pacienteId = perfil.pacienteId?.takeIf { it.isNotBlank() }
                val paciente by remember(pacienteId) {
                    pacienteId?.let(repository::observarPaciente) ?: flowOf(null)
                }.collectAsStateWithLifecycle(initialValue = null)
                val diagnosticos = paciente?.diagnosticos.orEmpty()
                val nombreSaludo = paciente?.nombre?.trim()?.takeIf { it.isNotBlank() }
                    ?: perfil.nombre.trim().ifBlank { "Paciente" }

                ContenidoActualizable(actualizando, onActualizar) {
                    InicioPacienteScreen(
                        nombre = nombreSaludo,
                        resumenCita = resumenProxima,
                        totalDiagnosticos = diagnosticos.size,
                        ultimoDiagnostico = diagnosticos.firstOrNull(),
                        irMisCitas = { navController.navigate("citas") { launchSingleTop = true } },
                        irMiHistorial = { pacienteId?.let(abrirHistorial) },
                        irDetalleCita = { id ->
                            if (id.isNotBlank()) {
                                agenda.limpiarError()
                                navController.navigate("cita/${Uri.encode(id)}") { launchSingleTop = true }
                            }
                        }
                    )
                }
            }
        }

        composable(INICIO) {
            if (!esMedico) {
                RutaRestringida(
                    mensaje = "Esta sección es exclusiva para personal médico.",
                    alRedirigir = redirigirAlInicio,
                    onVolver = volver
                )
            } else {
                val resumenProxima by agenda.resumenProxima.collectAsStateWithLifecycle()
                val busqueda: BuscarPacienteViewModel = viewModel { BuscarPacienteViewModel(repository) }
                val query by busqueda.query.collectAsStateWithLifecycle()
                val pacientes by busqueda.pacientes.collectAsStateWithLifecycle()
                ContenidoActualizable(actualizando, onActualizar) {
                    InicioScreen(
                        query = query,
                        pacientes = pacientes,
                        onQueryChange = busqueda::onQueryChange,
                        irAgregarPaciente = registrar,
                        irDetallesPaciente = abrirHistorial,
                        irAgenda = { navController.navigate("citas") { launchSingleTop = true } },
                        irCatalogo = { navController.navigate(CATALOGO_MEDICAMENTOS) { launchSingleTop = true } },
                        irDetalleCita = { id ->
                            agenda.limpiarError()
                            navController.navigate("cita/${Uri.encode(id)}") { launchSingleTop = true}
                        },
                        resumenCita = resumenProxima
                    )
                }
            }
        }

        composable(CATALOGO_MEDICAMENTOS) {
            if (!esMedico) {
                RutaRestringida(
                    mensaje = "Esta sección es exclusiva para personal médico.",
                    alRedirigir = redirigirAlInicio,
                    onVolver = volver
                )
            } else {
                val catalogo: CatalogoMedicamentosViewModel = viewModel {
                    CatalogoMedicamentosViewModel(CatalogoMedicamentosRepository(RetrofitClient.apiService))
                }
                val estado by catalogo.estado.collectAsStateWithLifecycle()
                ContenidoActualizable(estado is CatalogoMedicamentosEstado.Loading, catalogo::cargar) {
                    CatalogoMedicamentosScreen(estado, reintentar = catalogo::cargar, volver = volver)
                }
            }
        }

        composable("citas") {
            ContenidoActualizable(actualizando, onActualizar) {
                AgendaCitasScreen(datosAgenda, estadoAgenda,
                    onFiltro = agenda::seleccionarFiltro,
                    onNueva = {
                        if (esMedico) {
                            agenda.prepararFormulario()
                            navController.navigate("formulario_cita") { launchSingleTop = true }
                        }
                    },
                    onDetalle = { id ->
                        agenda.limpiarError()
                        navController.navigate("cita/${Uri.encode(id)}") { launchSingleTop = true }
                    }, onVolver = volver)
            }
        }
        composable("formulario_cita") { entrada ->
            if (!esMedico) {
                RutaRestringida(
                    mensaje = "Esta sección es exclusiva para personal médico.",
                    alRedirigir = redirigirAlInicio,
                    onVolver = volver
                )
            } else {
                // Mismo buscador que la pantalla de Inicio, con su propia instancia para este formulario
                val busqueda: BuscarPacienteViewModel = viewModel { BuscarPacienteViewModel(repository) }
                val consulta by busqueda.query.collectAsStateWithLifecycle()
                val coincidencias by busqueda.pacientes.collectAsStateWithLifecycle()
                FormularioCitaScreen(datosAgenda, estadoAgenda,
                    busqueda = consulta,
                    coincidencias = coincidencias,
                    onBusquedaChange = busqueda::onQueryChange,
                    onCambio = agenda::cambiarFormulario,
                    onGuardar = { agenda.guardar { if (navController.currentBackStackEntry == entrada) volver() } },
                    onVolver = volver)
            }
        }
        composable("cita/{citaId}", arguments = listOf(navArgument("citaId") { type = NavType.StringType })) { entrada ->
            val id = entrada.arguments?.getString("citaId").orEmpty()
            val detalle by remember(agenda, id) { agenda.observarDetalle(id) }
                .collectAsStateWithLifecycle(initialValue = DetalleCitaDatos())
            ContenidoActualizable(actualizando, onActualizar) {
                DetalleCitaScreen(id, detalle, datosAgenda.pacientes, estadoAgenda,
                    onEditar = { cita ->
                        if (esMedico) {
                            agenda.prepararFormulario(cita)
                            navController.navigate("formulario_cita") { launchSingleTop = true }
                        }
                    },
                    onResponder = { agenda.responder(id, it) },
                    onEliminar = {
                        if (esMedico) {
                            agenda.eliminar(id) { if (navController.currentBackStackEntry == entrada) volver() }
                        }
                    },
                    onVolver = volver)
            }
        }

        composable(REGISTRAR_PACIENTE) { entrada ->
            if (!esMedico) {
                RutaRestringida(
                    mensaje = "Esta sección es exclusiva para personal médico.",
                    alRedirigir = redirigirAlInicio,
                    onVolver = volver
                )
            } else {
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
        }

        composable(
            route = "$HISTORIAL_MEDICO/{$PACIENTE_ID}",
            arguments = listOf(navArgument(PACIENTE_ID) { type = NavType.StringType })
        ) { entrada ->
            val solicitado = entrada.arguments?.getString(PACIENTE_ID)
            val propioId = perfil.pacienteId?.takeIf { it.isNotBlank() }
            val autorizado = esMedico || (propioId != null && solicitado == propioId)
            if (!autorizado) {
                Scaffold(
                    topBar = { TopBarHistorial(onVolver = volver) },
                    containerColor = FondoBlanco
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes permiso para ver este historial médico.",
                            color = TextoOscuro
                        )
                    }
                }
            } else {
                val historial: HistorialViewModel = viewModel(
                    key = "historial-${perfil.uid}-${solicitado.orEmpty()}"
                ) {
                    HistorialViewModel(repository, solicitado)
                }
                val paciente by historial.paciente.collectAsStateWithLifecycle()
                ContenidoActualizable(actualizando, onActualizar) {
                    HistorialMedicoScreen(paciente = paciente, onVolver = volver)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RutaRestringida(
    mensaje: String,
    alRedirigir: () -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(Unit) {
        alRedirigir()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acceso restringido") },
                navigationIcon = { BotonVolver(onClick = onVolver, modifier = Modifier.padding(start = 8.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulPrincipal,
                    titleContentColor = FondoBlanco
                )
            )
        },
        containerColor = FondoBlanco
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = mensaje, color = TextoOscuro)
        }
    }
}
