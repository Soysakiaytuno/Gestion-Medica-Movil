package com.example.appmedica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmedica.model.Paciente

// Colores para el encabezado y el recuadro azul de datos del paciente
private val DarkNavyBlue = Color(0xFF0B2536)
private val BackButtonBg = Color(0xFF1E3A4C)
private val HeaderTitleColor = Color(0xFF6B8496)

@Composable
fun HistorialMedicoScreen(
    paciente: Paciente?,
    onVolver: () -> Unit
) {
    // Datos generales de ejemplo en caso de que aún no llegue un paciente por parámetro
    val pacienteActual = paciente ?: Paciente(
        nombre = "Josue",
        apellidoPaterno = "Balbontin",
        apellidoMaterno = "Perez",
        ci = "8493021",
        email = "josue.balbontin@email.com",
        celular = "70012345",
        diagnosticos = emptyList()
    )

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Únicamente la sección superior: Recuadro azul con los datos generales del paciente
            SeccionVistaPreviaPaciente(paciente = pacienteActual)
        }
    }
}

@Composable
fun SeccionVistaPreviaPaciente(paciente: Paciente) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recuadro azul oscuro con los datos generales del paciente
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(DarkNavyBlue)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${paciente.nombre} ${paciente.apellidoPaterno} ${paciente.apellidoMaterno}".trim(),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "CI: ${paciente.ci}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
                Text(
                    text = "Email: ${paciente.email}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Celular: ${paciente.celular}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Avatar a la derecha
        Box(
            modifier = Modifier
                .size(width = 86.dp, height = 110.dp)
                .clip(CircleShape)
                .background(Color(0xFF3E4A59)),
            contentAlignment = Alignment.Center
        ) {
            val iniciales = "${paciente.nombre.firstOrNull() ?: ""}${paciente.apellidoPaterno.firstOrNull() ?: ""}".uppercase()
            Text(
                text = iniciales.ifEmpty { "P" },
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
