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
import com.example.appmedica.ui.screens.BuscarPacienteScreen
import com.example.appmedica.ui.screens.RegistrarPacienteScreen
import com.example.appmedica.model.Paciente
import java.time.LocalDate

import java.time.Period

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DBClass.agregar(
            Paciente(
                nombre = "Jose",
                apellidoPaterno = "Mendoza",
                apellidoMaterno = "Rojas",
                ci = "1234567",
                email = "jose@mail.com",
                celular = "70000001",
                fechaNacimiento = LocalDate.of(2001, 3, 15)
            )
        )
        setContent {
            MaterialTheme {
                BuscarPacienteScreen()
            }
        }
    }
}

@Composable
fun AppMedica() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "buscar") {

        /*composable("buscar") {
            BuscarPacienteScreen(
                volver = { navController.popBackStack() },
                irAgregar = { navController.navigate("registro") },
                irDetalles = { id -> /* pendiente: navegación a detalle */ }
            )
        }*/

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
