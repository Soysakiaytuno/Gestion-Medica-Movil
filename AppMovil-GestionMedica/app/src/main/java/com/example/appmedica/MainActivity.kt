package com.example.appmedica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.navigation.AppNavigation
import com.example.appmedica.repository.PacienteRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val pacientes = PacienteRepository(AppDatabase.obtener(applicationContext))
        setContent {
            MaterialTheme {
                AppNavigation(pacientes)
            }
        }
    }
}
