package com.example.appmedica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmedica.R
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.ui.theme.AvatarInicio
import com.example.appmedica.ui.theme.AzulClaro
import com.example.appmedica.ui.theme.AzulOscuro
import com.example.appmedica.ui.theme.AzulPildora
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco
import com.example.appmedica.ui.theme.TextoOscuro
import com.example.appmedica.viewmodel.ProximaCita
import com.example.appmedica.viewmodel.ResumenProxima
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun InicioPacienteScreen(
    nombre: String,
    resumenCita: ResumenProxima,
    totalDiagnosticos: Int,
    ultimoDiagnostico: Diagnostico?,
    irMisCitas: () -> Unit,
    irMiHistorial: () -> Unit,
    irDetalleCita: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .testTag("lista_inicio_paciente")
            .fillMaxSize()
            .background(FondoBlanco)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            EncabezadoInicioPaciente(
                nombre = nombre,
                irMisCitas = irMisCitas,
                irMiHistorial = irMiHistorial
            )
        }

        item {
            Text(
                text = "Tu próxima cita",
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
            )
        }

        item {
            CardProximaCitaPaciente(
                resumen = resumenCita,
                irDetalleCita = irDetalleCita
            )
        }

        item {
            Text(
                text = "Resumen de mi historial",
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
            )
        }

        item {
            CardResumenHistorial(
                totalDiagnosticos = totalDiagnosticos,
                ultimoDiagnostico = ultimoDiagnostico,
                irMiHistorial = irMiHistorial
            )
        }
    }
}

@Composable
private fun EncabezadoInicioPaciente(
    nombre: String,
    irMisCitas: () -> Unit,
    irMiHistorial: () -> Unit
) {
    val nombreSaludo = nombre.trim().ifEmpty { "Paciente" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(AzulPrincipal)
            .padding(horizontal = 27.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fechaDeHoy(),
                    color = FondoBlanco,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¡Hola, $nombreSaludo!",
                    color = FondoBlanco,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tu portal de salud",
                    color = AzulClaro,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AvatarInicio),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Perfil del paciente",
                    tint = AzulPrincipal,
                    modifier = Modifier.size(35.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccesoRapidoPaciente(
                texto = "Mis citas",
                painter = painterResource(R.drawable.ic_agenda),
                modifier = Modifier.weight(1f),
                onClick = irMisCitas
            )
            AccesoRapidoPaciente(
                texto = "Mi historial médico",
                painter = rememberVectorPainter(Icons.AutoMirrored.Outlined.List),
                modifier = Modifier.weight(1f),
                onClick = irMiHistorial
            )
        }
    }
}

@Composable
private fun AccesoRapidoPaciente(
    texto: String,
    painter: Painter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(75.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(AzulPildora),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = AzulPrincipal,
                modifier = Modifier.size(45.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = texto,
            color = FondoBlanco,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CardProximaCitaPaciente(
    resumen: ResumenProxima,
    irDetalleCita: (String) -> Unit
) {
    val forma = RoundedCornerShape(24.dp)
    val baseModifier = Modifier
        .padding(horizontal = 20.dp)
        .fillMaxWidth()
        .clip(forma)
        .background(AzulClaro)
        .border(1.dp, AzulPildora, forma)

    val cita = resumen.cita
    if (cita == null) {
        Text(
            text = resumen.mensaje,
            color = AzulPrincipal,
            style = MaterialTheme.typography.bodyMedium,
            modifier = baseModifier.padding(horizontal = 22.dp, vertical = 18.dp)
        )
        return
    }

    Row(
        modifier = baseModifier
            .clickable { irDetalleCita(cita.citaId) }
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cita.motivo,
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = fechaCorta(cita.fecha),
                color = AzulOscuro,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = horaCorta(cita.hora),
                color = AzulPrincipal,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .background(AzulPrincipal)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ver detalles",
                        color = FondoBlanco,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = FondoBlanco,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardResumenHistorial(
    totalDiagnosticos: Int,
    ultimoDiagnostico: Diagnostico?,
    irMiHistorial: () -> Unit
) {
    val forma = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(forma)
            .background(FondoBlanco)
            .border(1.dp, AzulPildora, forma)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Diagnósticos registrados: $totalDiagnosticos",
            color = AzulPrincipal,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (ultimoDiagnostico != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AzulClaro)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Último diagnóstico",
                    color = AzulOscuro,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = ultimoDiagnostico.titulo,
                    color = TextoOscuro,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Fecha: ${ultimoDiagnostico.fecha}",
                    color = AzulOscuro,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Text(
                text = "Aún no tienes diagnósticos registrados.",
                color = AzulOscuro,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .background(AzulPrincipal)
                    .clickable(onClick = irMiHistorial)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ver historial completo",
                        color = FondoBlanco,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = FondoBlanco,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/** "08:30" → "8:30" */
private fun horaCorta(hora: String): String = runCatching {
    LocalTime.parse(hora).format(DateTimeFormatter.ofPattern("H:mm"))
}.getOrDefault(hora)

/** "2026-12-24" → "24 dic 2026" */
private fun fechaCorta(fecha: String): String = runCatching {
    LocalDate.parse(fecha)
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))
        .replace(".", "")
}.getOrDefault(fecha)

/** Ejemplo: "Lunes, 15 de Septiembre 2026" */
private fun fechaDeHoy(): String = runCatching {
    val es = Locale.forLanguageTag("es")
    val hoy = LocalDate.now()
    val dia = hoy.dayOfWeek.getDisplayName(TextStyle.FULL, es)
        .replaceFirstChar { it.uppercase() }
    val mes = hoy.month.getDisplayName(TextStyle.FULL, es)
        .replaceFirstChar { it.uppercase() }
    "$dia, ${hoy.dayOfMonth} de $mes ${hoy.year}"
}.getOrDefault("")

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioPacienteScreenPreview() {
    MaterialTheme {
        InicioPacienteScreen(
            nombre = "Josue",
            resumenCita = ResumenProxima(
                cita = ProximaCita(
                    citaId = "cita-1",
                    paciente = "Balbontin Josue",
                    motivo = "Control respiratorio general",
                    fecha = "2026-12-24",
                    hora = "09:30"
                ),
                mensaje = ""
            ),
            totalDiagnosticos = 2,
            ultimoDiagnostico = Diagnostico(
                id = "diag-1",
                fecha = "04/10/2026",
                titulo = "Asma",
                descripcion = "Seguimiento de tratamiento inhalatorio."
            ),
            irMisCitas = {},
            irMiHistorial = {},
            irDetalleCita = {}
        )
    }
}
