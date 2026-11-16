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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.calcularEdad
import com.example.appmedica.ui.theme.AzulClaro
import com.example.appmedica.ui.theme.AzulMedio
import com.example.appmedica.ui.theme.AzulPildora
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco
import com.example.appmedica.ui.theme.FondoBusquedaInicio
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun InicioScreen(
    query: String,
    pacientes: List<Paciente>,
    onQueryChange: (String) -> Unit,
    irAgregarPaciente: () -> Unit = {},
    irDetallesPaciente: (String) -> Unit = {},
    irAgenda: () -> Unit = {},
    irCatalogo: () -> Unit = {},
    resumenCita: String = "No hay próximas citas."
) {
    LazyColumn(
        modifier = Modifier
            .testTag("lista_inicio")
            .fillMaxSize()
            .background(FondoBlanco)
            .navigationBarsPadding()
            .imePadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Encabezado oscuro con saludo y accesos rápidos
        item {
            EncabezadoInicio(
                irAgenda = irAgenda,
                irRecetar = irCatalogo,
                irAgregarPaciente = irAgregarPaciente
            )
        }
        
        // Próxima consulta
        item {
            Text(
                text = "Próxima consulta",
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
            )
        }
        item {
            Text(
                text = resumenCita,
                color = AzulPrincipal,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(AzulClaro)
                    .padding(16.dp)
            )
        }

        // Buscador
        item {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(text = "Buscar por nombre", color = AzulMedio) },
                trailingIcon = { Text(text = "\uD83D\uDD0D") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FondoBusquedaInicio,
                    unfocusedContainerColor = FondoBusquedaInicio,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AzulPrincipal,
                    focusedTextColor = AzulPrincipal,
                    unfocusedTextColor = AzulPrincipal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )
        }

        // Lista de pacientes
        item {
            Text(
                text = "Pacientes",
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
            )
        }

        if (pacientes.isEmpty()) {
            item {
                Text(
                    text = "No se encontraron pacientes",
                    color = AzulMedio,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        items(items = pacientes, key = { it.id }) { paciente ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .fillMaxWidth()
                    .border(1.dp, AzulPildora)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nombre: ${paciente.nombre} ${paciente.apellidoPaterno}",
                        color = AzulPrincipal,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Edad: ${calcularEdad(paciente.fechaNacimiento)} años",
                        color = AzulPrincipal,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AzulClaro)
                        .heightIn(min = 48.dp)
                        .clickable { irDetallesPaciente(paciente.id) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Más detalles", color = AzulPrincipal, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EncabezadoInicio(
    irAgenda: () -> Unit,
    irRecetar: () -> Unit,
    irAgregarPaciente: () -> Unit,
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(AzulPrincipal)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = fechaDeHoy(), color = FondoBlanco, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${saludoSegunHora()}, Dr. Jaldin",
                    color = FondoBlanco,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccesoRapido("Agenda Consultas", Modifier.weight(1f), irAgenda)
            AccesoRapido("Recetar", Modifier.weight(1f), irRecetar)
            AccesoRapido("Agregar paciente", Modifier.weight(1f), irAgregarPaciente)
        }
    }
}

@Composable
private fun AccesoRapido(texto: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(AzulPildora)
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = texto,
            color = AzulPrincipal,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// ---------- Utilidades de fecha y saludo ----------

/** Ejemplo: "Lunes, 15 de Septiembre 2026" */
private fun fechaDeHoy(): String {
    val es = Locale.forLanguageTag("es")
    val hoy = LocalDate.now()
    val dia = hoy.dayOfWeek.getDisplayName(TextStyle.FULL, es)
        .replaceFirstChar { it.uppercase() }
    val mes = hoy.month.getDisplayName(TextStyle.FULL, es)
        .replaceFirstChar { it.uppercase() }
    return "$dia, ${hoy.dayOfMonth} de $mes ${hoy.year}"
}
/** "Buen día" (5–11 h), "Buenas tardes" (12–18 h) o "Buenas noches" */
private fun saludoSegunHora(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Buen día"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioScreenPreview() {
    MaterialTheme {
        InicioScreen(query = "", pacientes = emptyList(), onQueryChange = {})
    }
}
