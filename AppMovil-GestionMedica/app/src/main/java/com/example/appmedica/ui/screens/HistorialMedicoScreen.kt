package com.example.appmedica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.Paciente
import com.example.appmedica.viewmodel.HistorialViewModel

// Paleta de colores basada en el diseño de referencia
private val DarkNavyBlue = Color(0xFF0B2536)
private val BackButtonBg = Color(0xFF1E3A4C)
private val HeaderTitleColor = Color(0xFF6B8496)
private val PillBlueGray = Color(0xFF96B3C4)
private val LightBlueCard = Color(0xFFD6EEFF)
private val TextDarkColor = Color(0xFF1A1A1A)

@Composable
fun HistorialMedicoScreen(
    paciente: Paciente?,
    onVolver: () -> Unit,
    pacienteId: String? = null,
    viewModel: HistorialViewModel = remember { HistorialViewModel() }
) {
    // Sincronizamos los datos del paciente específico y sus diagnósticos desde la base de datos simulada
    LaunchedEffect(paciente, pacienteId) {
        viewModel.cargarDatosHistorial(paciente, pacienteId)
    }

    val pacienteActual = viewModel.pacienteActual
    val listaDiagnosticos = viewModel.listaDiagnosticos

    Scaffold(
        topBar = {
            TopBarHistorial(onVolver = onVolver)
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (pacienteActual != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // 1. Encabezado con la vista previa completa del paciente
                item {
                    SeccionVistaPreviaPaciente(
                        paciente = pacienteActual,
                        totalConsultas = listaDiagnosticos.size
                    )
                }

                // 2. Lista de diagnósticos (si tiene diagnósticos registrados)
                if (listaDiagnosticos.isNotEmpty()) {
                    items(
                        items = listaDiagnosticos,
                        key = { diagnostico -> diagnostico.id }
                    ) { diagnostico ->
                        ItemDiagnosticoCard(diagnostico = diagnostico)
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(LightBlueCard)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Este paciente aún no tiene diagnósticos registrados.",
                                color = TextDarkColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró información del paciente.",
                    color = TextDarkColor,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun TopBarHistorial(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkNavyBlue)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BackButtonBg)
                .clickable { onVolver() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Historial medico",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SeccionVistaPreviaPaciente(
    paciente: Paciente,
    totalConsultas: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recuadro azul oscuro con todos los datos del paciente como vista previa
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
                Text(
                    text = "Fecha Nac.: ${paciente.fechaNacimiento}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
                Text(
                    text = "Consulta $totalConsultas",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
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

/**
 * Componente reutilizable que representa un diagnóstico individual
 * con su título en píldora, recuadro de fecha y descripción, y línea divisoria.
 */
@Composable
fun ItemDiagnosticoCard(diagnostico: Diagnostico) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Píldora superior con el título del diagnóstico
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .background(PillBlueGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = diagnostico.titulo,
                color = TextDarkColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 2. Recuadro azul claro con la fecha y descripción
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(LightBlueCard)
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Fecha: ${diagnostico.fecha}",
                    color = TextDarkColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = diagnostico.descripcion,
                    color = TextDarkColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }

        // 3. Línea divisoria inferior
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            thickness = 1.5.dp,
            color = Color(0xFF4A4A4A)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistorialMedicoScreenPreview() {
    MaterialTheme {
        HistorialMedicoScreen(
            paciente = null,
            onVolver = {}
        )
    }
}
