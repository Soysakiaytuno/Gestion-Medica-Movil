package com.example.appmedica.navigator

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmedica.db.DBClass
import com.example.appmedica.ui.screens.BuscarPacienteScreen
import com.example.appmedica.ui.screens.HistorialMedicoScreen
import com.example.appmedica.ui.screens.InicioScreen
import com.example.appmedica.ui.screens.RegistrarPacienteScreen

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "inicio"
    ) {
        // Pantalla Principal (Inicio)
        composable("inicio") {
            InicioScreen(
                irBuscarPaciente = {
                    navController.navigate("buscar_paciente") { launchSingleTop = true }
                },
                irAgregarPaciente = {
                    navController.navigate("registrar_paciente") { launchSingleTop = true }
                },
                irDetallesPaciente = { pacienteId ->
                    navController.navigate("historial_medico/$pacienteId") { launchSingleTop = true }
                }
            )
        }

        // Pantalla de Búsqueda / Selección de Pacientes
        composable("buscar_paciente") {
            BuscarPacienteScreen(
                volver = { navController.popBackStack() },
                irAgregar = {
                    navController.navigate("registrar_paciente") { launchSingleTop = true }
                },
                irDetalles = { pacienteId ->
                    navController.navigate("historial_medico/$pacienteId") { launchSingleTop = true }
                }
            )
        }

        // Pantalla para Registrar Nuevo Paciente
        composable("registrar_paciente") {
            RegistrarPacienteScreen(
                onGuardarPaciente = { nuevoPaciente ->
                    DBClass.agregar(nuevoPaciente)
                    navController.popBackStack()
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // Pantalla de Historial Médico con el paciente seleccionado específico por ID
        composable(
            route = "historial_medico/{pacienteId}",
            arguments = listOf(
                navArgument("pacienteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("pacienteId")
            val paciente = pacienteId?.let(DBClass::getPacientePorId)

            HistorialMedicoScreen(
                paciente = paciente,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
