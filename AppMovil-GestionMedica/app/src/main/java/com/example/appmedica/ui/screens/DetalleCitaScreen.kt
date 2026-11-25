package com.example.appmedica.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmedica.model.*
import com.example.appmedica.ui.theme.*
import com.example.appmedica.viewmodel.CitasEstado
import com.example.appmedica.viewmodel.DetalleCitaDatos
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DetalleCitaScreen(
    id: String, detalle: DetalleCitaDatos, pacientes: List<Paciente>, estado: CitasEstado,
    onEditar: (Cita) -> Unit, onResponder: (EstadoCita) -> Unit,
    onEliminar: () -> Unit, onVolver: () -> Unit
) {
    val cita = detalle.cita
    val esMedico = estado.rol == RolUsuario.MEDICO
    val autorizada = esMedico || (!estado.pacienteId.isNullOrBlank() && estado.pacienteId == cita?.pacienteId)
    val visible = cita?.takeIf { autorizada && !detalle.cargando && detalle.error.isEmpty() }
    var confirmar by rememberSaveable(id) { mutableStateOf(false) }
    val acciones: (@Composable ColumnScope.() -> Unit)? = if (visible != null) {
        {
            MensajeOperacion(estado)
            if (esMedico) {
                OutlinedButton(
                    onClick = { onEditar(visible) },
                    enabled = !estado.ocupado && visible.estado == EstadoCita.PENDIENTE,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                        .semantics { contentDescription = "Editar cita" },
                    shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, AzulOscuro)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Editar cita", fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = { confirmar = true }, enabled = !estado.ocupado,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .semantics { contentDescription = "Eliminar cita" },
                    colors = ButtonDefaults.textButtonColors(contentColor = RojoEliminar)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar cita")
                }
            } else if (visible.estado == EstadoCita.PENDIENTE) {
                Button(
                    onClick = { onResponder(EstadoCita.ACEPTADA) }, enabled = !estado.ocupado,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(14.dp)
                ) { Text("Aceptar cita", fontWeight = FontWeight.SemiBold) }
                OutlinedButton(
                    onClick = { onResponder(EstadoCita.RECHAZADA) }, enabled = !estado.ocupado,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(14.dp)
                ) { Text("Rechazar cita") }
            }
        }
    } else null

    MarcoCitas("Detalle de Cita", onVolver, botones = acciones) {
        when {
            detalle.cargando -> CircularProgressIndicator()
            detalle.error.isNotBlank() -> Text(detalle.error, color = TextoError)
            cita == null -> Text("La cita ya no está disponible.")
            !autorizada -> Text("Esta cita no te corresponde.")
            else -> {
                val paciente = pacientes.find { it.id == cita.pacienteId }
                val nombre = paciente?.let { nombrePaciente(it, incluirMaterno = true) } ?: "Paciente"
                Surface(color = AzulPrincipal, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Surface(color = FondoAvatarHistorial, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(nombre.split(" ").filter { it.isNotBlank() }.take(2)
                                        .joinToString("") { it.take(1) }.uppercase(),
                                        color = FondoBlanco, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("PACIENTE", color = AzulPildora, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
                                Text(nombre, color = FondoBlanco, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(color = AzulClaro, shape = RoundedCornerShape(50.dp)) {
                            Text(cita.estado.etiqueta, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                color = AzulPrincipal, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DatoCita("FECHA", fechaPresentacion(cita.fecha), Modifier.weight(1f))
                    DatoCita("HORA", cita.hora, Modifier.weight(1f))
                }
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = FondoBlanco),
                    border = BorderStroke(1.dp, AzulPildora.copy(alpha = .6f))
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Motivo de la consulta", color = AzulPrincipal,
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(cita.motivo, color = TextoOscuro, style = MaterialTheme.typography.bodyLarge, lineHeight = 25.sp)
                    }
                }
                Text(
                    when (cita.estado) {
                        EstadoCita.PENDIENTE -> if (esMedico) "La propuesta está pendiente de respuesta del paciente."
                            else "Confirma si puedes asistir a esta consulta."
                        EstadoCita.ACEPTADA -> if (esMedico) "El paciente confirmó su asistencia." else "Tu asistencia está confirmada."
                        EstadoCita.RECHAZADA -> if (esMedico) "El paciente rechazó esta propuesta." else "Tu respuesta ya fue registrada."
                    },
                    modifier = Modifier.padding(horizontal = 4.dp), color = AzulMedio, style = MaterialTheme.typography.bodyMedium
                )
                if (cita.estado != EstadoCita.PENDIENTE && esMedico) {
                    Text("Solo se pueden editar citas pendientes.", color = AzulMedio, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    if (confirmar && visible != null && esMedico) {
        AlertDialog(
            containerColor = FondoBlanco, titleContentColor = AzulPrincipal, textContentColor = TextoOscuro,
            onDismissRequest = { confirmar = false }, title = { Text("¿Eliminar esta cita?") },
            text = { Text("Se eliminará la cita del ${fechaPresentacion(visible.fecha)} a las ${visible.hora}. Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = { confirmar = false; onEliminar() }, enabled = !estado.ocupado,
                colors = ButtonDefaults.textButtonColors(contentColor = RojoEliminar)) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmar = false },
                colors = ButtonDefaults.textButtonColors(contentColor = AzulOscuro)) { Text("Conservar") } }
        )
    }
}

@Composable
private fun DatoCita(titulo: String, valor: String, modifier: Modifier) {
    Surface(color = FondoTarjetaDiagnostico, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, color = AzulMedio, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
            Text(valor, color = AzulPrincipal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun fechaPresentacion(fecha: String): String = runCatching {
    LocalDate.parse(fecha).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}.getOrDefault(fecha)
