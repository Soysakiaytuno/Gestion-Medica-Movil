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
                irAgendarCita = { navController.navigate("buscar_paciente") },
                irReceta = { navController.navigate("buscar_paciente") },
                irAgregarPaciente = { navController.navigate("registrar_paciente") },
                irDetallesPaciente = { pacienteId ->
                    navController.navigate("historial_medico/$pacienteId")
                }
            )
        }

        // Pantalla de Búsqueda / Selección de Pacientes
        composable("buscar_paciente") {
            BuscarPacienteScreen(
                volver = { navController.popBackStack() },
                irAgregar = { navController.navigate("registrar_paciente") },
                irDetalles = { pacienteId ->
                    navController.navigate("historial_medico/$pacienteId")
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
            val pacienteId = backStackEntry.arguments?.getString("pacienteId") ?: ""
            val paciente = DBClass.getPacientePorId(pacienteId) ?: DBClass.getPaciente(pacienteId)

            HistorialMedicoScreen(
                paciente = paciente,
                pacienteId = pacienteId,
                onVolver = { navController.popBackStack() }
            )
        }

        // Ruta de respaldo para historial médico
        composable("historial_medico") {
            HistorialMedicoScreen(
                paciente = DBClass.getPacientes().firstOrNull(),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
