package com.example.appmedica.navigator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.appmedica.db.DBClass
import com.example.appmedica.ui.screens.BuscarPacienteScreen
import com.example.appmedica.ui.screens.RegistrarPacienteScreen
import com.example.appmedica.ui.screens.HistorialMedicoScreen

@Composable
fun AppNavegacion(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "buscar_paciente"
    ){
        composable("buscar_paciente"){
            BuscarPacienteScreen(
                volver = {navController.popBackStack()},
                irAgregar = { navController.navigate("registrar_paciente") },
                irDetalles = { pacienteId -> navController.navigate("historial_medico")}
            )
        }

        composable("registrar_paciente"){
            RegistrarPacienteScreen(
                onGuardarPaciente = {nuevoPaciente ->
                    DBClass.agregar(nuevoPaciente)
                    navController.popBackStack()
                },
                onVolver = {navController.popBackStack()}
            )
        }

        composable("historial_medico"){
            HistorialMedicoScreen(
                paciente = null,
                onVolver = {navController.popBackStack()}
            )
        }
    }
}
