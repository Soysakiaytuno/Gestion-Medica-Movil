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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmedica.model.calcularEdad
import com.example.appmedica.viewmodel.BuscarPacienteViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

private val CampoBusqueda = Color(0xFFC9DBE7)
private val AvatarGris = Color(0xFFD9D9D9)

/** Datos de la próxima consulta (aún no existe un modelo de citas en el proyecto). */
data class ConsultaProxima(
    val paciente: String,
    val motivo: String,
    val hora: String,
    val lugar: String
)

@Composable
fun InicioScreen(
    nombreDoctor: String = "Dr. Jaldín",
    consultaProxima: ConsultaProxima? = ConsultaProxima(
        paciente = "Joe Jaldin",
        motivo = "Resfrío",
        hora = "8:30",
        lugar = "Consultorio medico"
    ),
    irAgendarCita: () -> Unit = {},
    irReceta: () -> Unit = {},
    irAgregarPaciente: () -> Unit = {},
    irDetallesConsulta: () -> Unit = {},
    irDetallesPaciente: (String) -> Unit = {},
    viewModel: BuscarPacienteViewModel = viewModel()
) {
    val pacientes = viewModel.pacientesFiltrados()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Encabezado oscuro con saludo y accesos rápidos
        item {
            EncabezadoInicio(
                nombreDoctor = nombreDoctor,
                irAgendarCita = irAgendarCita,
                irReceta = irReceta,
                irAgregarPaciente = irAgregarPaciente
            )
        }

        // Próxima consulta
        item {
            Text(
                text = "Proxima Consulta",
                color = AzulPrincipal,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
            )
        }
        item {
            if (consultaProxima != null) {
                TarjetaConsulta(
                    consulta = consultaProxima,
                    onVerDetalles = irDetallesConsulta,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            } else {
                Text(
                    text = "No hay consultas programadas",
                    color = AzulMedio,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // Buscador
        item {
            TextField(
                value = viewModel.query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text(text = "Buscar por nombre", color = AzulMedio) },
                trailingIcon = { Text(text = "\uD83D\uDD0D") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CampoBusqueda,
                    unfocusedContainerColor = CampoBusqueda,
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
                        .clickable { irDetallesPaciente(paciente.id) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Mas detalles", color = AzulPrincipal, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EncabezadoInicio(
    nombreDoctor: String,
    irAgendarCita: () -> Unit,
    irReceta: () -> Unit,
    irAgregarPaciente: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(AzulPrincipal)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = fechaDeHoy(), color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${saludo()}, $nombreDoctor",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // Espacio para la foto de perfil del doctor
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AvatarGris)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccesoRapido("Agendar cita", irAgendarCita, Modifier.weight(1f))
            AccesoRapido("Receta", irReceta, Modifier.weight(1f))
            AccesoRapido("Agregar paciente", irAgregarPaciente, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AccesoRapido(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AzulPildora)
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = texto,
            color = AzulPrincipal,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TarjetaConsulta(
    consulta: ConsultaProxima,
    onVerDetalles: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AzulClaro)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = consulta.paciente,
                color = AzulPrincipal,
                fontWeight = FontWeight.Medium
            )
            Text(text = consulta.motivo, color = AzulPrincipal, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AzulOscuro)
                    .clickable { onVerDetalles() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ver detalles", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = consulta.hora,
                color = AzulPrincipal,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = consulta.lugar, color = AzulPrincipal, fontSize = 13.sp)
        }
    }
}

// ---------- Utilidades de fecha y saludo ----------

private fun saludo(): String {
    val hora = LocalTime.now().hour
    return when {
        hora < 12 -> "Buenos días"
        hora < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

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
        InicioScreen()
    }
}
