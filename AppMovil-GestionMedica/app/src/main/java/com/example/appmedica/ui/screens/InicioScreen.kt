package com.example.appmedica.ui.screens

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.appmedica.R
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.calcularEdad
import com.example.appmedica.ui.theme.AzulClaro
import com.example.appmedica.ui.theme.AzulMedio
import com.example.appmedica.ui.theme.AzulPildora
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco
import com.example.appmedica.ui.theme.FondoBusquedaInicio
import com.example.appmedica.ui.theme.AvatarInicio
import com.example.appmedica.viewmodel.ResumenProxima
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter
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
    irDetalleCita: (String) -> Unit = {},
    resumenCita: ResumenProxima = ResumenProxima(mensaje = "No hay próximas citas.")
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
        
        // Próxima cita
        item {
            Text(
                text = "Próxima cita",
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 5.dp)
            )
        }
        item {
            CardProximaCita(resumen = resumenCita, irDetalleCita = irDetalleCita)
        }

        // Lista de pacientes
        item {
            Text(
                text = "Pacientes",
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp)
            )
        }

        // Buscador
        item {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(text = "Buscar nombre", color = AzulMedio) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        tint = AzulPrincipal
                    )
                },
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
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
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
                    .border(1.dp, AzulPildora, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
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
                    modifier = Modifier.minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(50))
                        .background(AzulClaro)
                        .clickable { irDetallesPaciente(paciente.id) }
                        .padding(horizontal = 15.dp, vertical = 5.dp),
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
            .padding(horizontal = 27.dp, vertical = 27.dp)
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AvatarInicio),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Perfil del médico",
                    tint = AzulPrincipal,
                    modifier = Modifier.size(35.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            AccesoRapido("Agenda de Citas", R.drawable.ic_agenda, Modifier.weight(1f), irAgenda)
            AccesoRapido("Recetar", R.drawable.ic_recetar, Modifier.weight(1f), irRecetar)
            AccesoRapido("Agregar paciente", R.drawable.ic_agregar_paciente, Modifier.weight(1f), irAgregarPaciente)
        }
    }
}

@Composable
private fun AccesoRapido(
    texto: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cuadrado redondeado solo con el ícono
        Box(
            modifier = Modifier
                .size(75.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(AzulPildora),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = AzulPrincipal,
                modifier = Modifier.size(45.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Texto blanco debajo; el ancho máximo hace que "Agenda de Citas" y "Agregar paciente" vayan en 2 líneas
        Text(
            text = texto,
            color = FondoBlanco,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 84.dp)
        )
    }
}

@Composable
private fun CardProximaCita(resumen: ResumenProxima, irDetalleCita: (String) -> Unit) {
    val modificadorCard = Modifier
        .padding(horizontal = 20.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(AzulClaro)
        .padding(horizontal = 22.dp, vertical = 16.dp)

    val cita = resumen.cita
    if (cita == null) {
        Text(text = resumen.mensaje, color = AzulPrincipal, modifier = modificadorCard)
        return
    }

    Row(modifier = modificadorCard) {
        // Izquierda: paciente y motivo
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = cita.paciente,
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cita.motivo,
                color = AzulPrincipal,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(30.dp))

        // Derecha: hora, fecha y botón
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = horaCorta(cita.hora),
                color = AzulPrincipal,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = fechaCorta(cita.fecha),
                color = AzulPrincipal,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .background(AzulPrincipal)
                    .clickable { irDetalleCita(cita.citaId) }
                    .padding(horizontal = 15.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Ver detalles", color = FondoBlanco, fontSize = 12.sp)
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

// ---------- Utilidades de fecha y saludo ----------
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
