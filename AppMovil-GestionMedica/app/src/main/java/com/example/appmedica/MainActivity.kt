package com.example.appmedica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appmedica.db.DBClass
import com.example.appmedica.ui.screens.RegistrarPacienteScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMedica()
                }
            }
        }
    }
}

@Composable
fun AppMedica() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "buscar") {

        composable("buscar") {
            BuscarPacienteScreen(
                volver = { navController.popBackStack() },
                irAgregar = { navController.navigate("registro") },
                irDetalles = { id -> /* pendiente: navegación a detalle */ }
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
    }
}
