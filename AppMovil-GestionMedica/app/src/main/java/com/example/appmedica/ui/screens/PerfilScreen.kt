package com.example.appmedica.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.model.calcularEdad
import com.example.appmedica.ui.components.BotonVolver
import com.example.appmedica.ui.theme.AzulClaro
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    perfil: PerfilUsuario,
    paciente: Paciente?,
    cerrarSesion: () -> Unit,
    volver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = { BotonVolver(onClick = volver, modifier = Modifier.padding(start = 8.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulPrincipal,
                    titleContentColor = FondoBlanco
                )
            )
        },
        containerColor = FondoBlanco
    ) { padding ->
        Column(
            // Mismas medidas que los botones fijos de Registrar Paciente y Citas
            modifier = Modifier.fillMaxSize().padding(padding).padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 48.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AzulClaro, contentColor = AzulPrincipal)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Nombre", fontWeight = FontWeight.Bold); Text(perfil.nombre)
                    Spacer(Modifier.height(8.dp))
                    Text("Correo", fontWeight = FontWeight.Bold); Text(perfil.correo)
                    Spacer(Modifier.height(8.dp))
                    Text("Rol", fontWeight = FontWeight.Bold)
                    if (perfil.rol == RolUsuario.MEDICO) {
                        Text("Médico")
                    } else {
                        Text("Paciente")
                    }

                    // Solo el paciente tiene ficha; para el médico paciente llega como null
                    if (paciente != null) {
                        Spacer(Modifier.height(8.dp))
                        Text("CI", fontWeight = FontWeight.Bold); Text(paciente.ci)
                        Spacer(Modifier.height(8.dp))
                        Text("Celular", fontWeight = FontWeight.Bold); Text(paciente.celular)
                        Spacer(Modifier.height(8.dp))
                        Text("Fecha de nacimiento", fontWeight = FontWeight.Bold); Text(paciente.fechaNacimiento.toString())
                        Spacer(Modifier.height(8.dp))
                        Text("Edad", fontWeight = FontWeight.Bold); Text("${calcularEdad(paciente.fechaNacimiento)} años")
                    }
                }
            }

            // Empuja el botón hasta la parte de abajo de la pantalla
            Spacer(Modifier.weight(1f))

            // Mismo estilo que tenía el botón en la barra de sesión
            OutlinedButton(
                onClick = cerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
