package com.example.appmedica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.navigation.AppNavigation
import com.example.appmedica.repository.PacienteRepository
import com.example.appmedica.repository.CitaRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.obtener(applicationContext)
        val pacientes = PacienteRepository(database)
        val citas = CitaRepository(database)
        setContent {
            MaterialTheme {
                AppNavigation(pacientes, citas)
            }
        }
    }
}
