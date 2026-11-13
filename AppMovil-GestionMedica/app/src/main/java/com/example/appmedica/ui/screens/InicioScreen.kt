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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.calcularEdad
import com.example.appmedica.ui.theme.AzulClaro
import com.example.appmedica.ui.theme.AzulMedio
import com.example.appmedica.ui.theme.AzulOscuro
import com.example.appmedica.ui.theme.AzulPildora
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco
import com.example.appmedica.ui.theme.FondoBusquedaInicio
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun InicioScreen(
    query: String,
    pacientes: List<Paciente>,
    onQueryChange: (String) -> Unit,
    irBuscarPaciente: () -> Unit = {},
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
                irBuscarPaciente = irBuscarPaciente,
                irAgregarPaciente = irAgregarPaciente,
                irAgenda = irAgenda
            )
        }

        item {
            TextButton(
                onClick = irCatalogo,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text("Catálogo de medicamentos · openFDA", color = AzulOscuro)
            }
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
    irBuscarPaciente: () -> Unit,
    irAgregarPaciente: () -> Unit,
    irAgenda: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(AzulPrincipal)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text("AppMedica", color = FondoBlanco, style = MaterialTheme.typography.headlineSmall)
        Text("Demostración académica · Datos locales ficticios", color = FondoBlanco,
            style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = fechaDeHoy(), color = FondoBlanco, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccesoRapido("Buscar paciente", Modifier.weight(1f), irBuscarPaciente)
            AccesoRapido("Agregar paciente", Modifier.weight(1f), irAgregarPaciente)
        }

        Spacer(modifier = Modifier.height(12.dp))
        AccesoRapido("Agenda de citas · Médico / Paciente demo", Modifier.fillMaxWidth(), irAgenda)
    }
}

@Composable
private fun AccesoRapido(texto: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AzulPildora)
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = texto,
            color = AzulPrincipal,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioScreenPreview() {
    MaterialTheme {
        InicioScreen(query = "", pacientes = emptyList(), onQueryChange = {})
    }
}
