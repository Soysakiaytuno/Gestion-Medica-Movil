package com.example.appmedica.navigator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.appmedica.ui.screens.BuscarPacienteScreen

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
    }
}
