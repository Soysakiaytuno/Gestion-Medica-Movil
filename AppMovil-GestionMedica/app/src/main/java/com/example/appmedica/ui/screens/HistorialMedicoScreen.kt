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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.Paciente
import com.example.appmedica.ui.theme.DivisorHistorial
import com.example.appmedica.ui.theme.FondoAvatarHistorial
import com.example.appmedica.ui.theme.FondoBlanco
import com.example.appmedica.ui.theme.FondoEncabezadoHistorial
import com.example.appmedica.ui.theme.FondoTarjetaDiagnostico
import com.example.appmedica.ui.theme.FondoTituloDiagnostico
import com.example.appmedica.ui.theme.FondoVolverHistorial
import com.example.appmedica.ui.theme.TextoOscuro

@Composable
fun HistorialMedicoScreen(
    paciente: Paciente?,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBarHistorial(onVolver = onVolver)
        },
        containerColor = FondoBlanco
    ) { paddingValues ->
        if (paciente != null) {
            val listaDiagnosticos = paciente.diagnosticos
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
                        paciente = paciente,
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
                                .background(FondoTarjetaDiagnostico)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Este paciente aún no tiene diagnósticos registrados.",
                                color = TextoOscuro,
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
                    color = TextoOscuro,
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
            .background(FondoEncabezadoHistorial)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FondoVolverHistorial)
                .clickable { onVolver() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = FondoBlanco,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Historial medico",
            color = FondoBlanco,
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
                .background(FondoEncabezadoHistorial)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${paciente.nombre} ${paciente.apellidoPaterno} ${paciente.apellidoMaterno}".trim(),
                    color = FondoBlanco,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "CI: ${paciente.ci}",
                    color = FondoBlanco.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
                Text(
                    text = "Email: ${paciente.email}",
                    color = FondoBlanco.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Celular: ${paciente.celular}",
                    color = FondoBlanco.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
                Text(
                    text = "Fecha Nac.: ${paciente.fechaNacimiento}",
                    color = FondoBlanco.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
                Text(
                    text = "Consulta $totalConsultas",
                    color = FondoBlanco,
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
                .background(FondoAvatarHistorial),
            contentAlignment = Alignment.Center
        ) {
            val iniciales = "${paciente.nombre.firstOrNull() ?: ""}${paciente.apellidoPaterno.firstOrNull() ?: ""}".uppercase()
            Text(
                text = iniciales.ifEmpty { "P" },
                color = FondoBlanco,
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
                .background(FondoTituloDiagnostico),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = diagnostico.titulo,
                color = TextoOscuro,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 2. Recuadro azul claro con la fecha y descripción
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(FondoTarjetaDiagnostico)
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Fecha: ${diagnostico.fecha}",
                    color = TextoOscuro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = diagnostico.descripcion,
                    color = TextoOscuro,
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
            color = DivisorHistorial
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
