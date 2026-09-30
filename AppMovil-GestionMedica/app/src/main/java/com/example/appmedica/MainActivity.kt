package com.example.appmedica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppMedica()
            }
        }
    }
}

@Composable
fun AppMedica() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "inicio") {

        composable("inicio") {
            InicioScreen(
                irAgregarPaciente = { navController.navigate("registro") },
                irDetallesPaciente = { id -> navController.navigate("historial/$id") }
                // irAgendarCita, irReceta e irDetallesConsulta: pendientes (sin pantalla aún)
            )
        }

        composable("buscar") {
            BuscarPacienteScreen(
                volver = { navController.popBackStack() },
                irAgregar = { navController.navigate("registro") },
                irDetalles = { id -> navController.navigate("historial/$id") }
            )
        }

        composable("registro") {
            RegistrarPacienteScreen(
                onGuardarPaciente = { nuevoPaciente ->
                    DBClass.agregar(nuevoPaciente)
                    navController.popBackStack()
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = "historial/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val paciente = DBClass.getPacientes().find { it.id == id }
            HistorialMedicoScreen(
                paciente = paciente,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
