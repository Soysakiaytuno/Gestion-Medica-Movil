package com.example.appmedica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appmedica.model.Paciente
import com.example.appmedica.ui.screens.*

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
    // La lista global que exige la rúbrica para compartir datos
    val pacientesGlobales = remember { mutableStateListOf<Paciente>() }

    NavHost(navController = navController, startDestination = "historial") {

        composable("inicio") {
            InicioScreen(
                listaPacientes = pacientesGlobales,
                onAgregarPaciente = { navController.navigate("registro") },
                onVerDetalle = { id -> navController.navigate("detalle/$id") }
            )
        }

        composable("registro") {
            RegistrarPacienteScreen(
                onGuardarPaciente = { nuevoPaciente ->
                    pacientesGlobales.add(nuevoPaciente)
                    navController.popBackStack()
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("detalle/{id}") {
            DetallePacienteScreen(
                paciente = pacientesGlobales.firstOrNull(),
                onVerHistorial = { navController.navigate("historial") },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("historial") {
            HistorialMedicoScreen(
                paciente = pacientesGlobales.firstOrNull(),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}