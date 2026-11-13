package com.example.appmedica.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmedica.model.*
import com.example.appmedica.ui.theme.*
import com.example.appmedica.viewmodel.AgendaDatos
import com.example.appmedica.viewmodel.CitasEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarcoCitas(titulo: String, volver: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    // Tema local que reutiliza la paleta; no modifica las pantallas existentes.
    MaterialTheme(colorScheme = lightColorScheme(
        primary = AzulOscuro, onPrimary = FondoBlanco,
        primaryContainer = AzulClaro, onPrimaryContainer = AzulPrincipal,
        secondaryContainer = AzulPildora, onSecondaryContainer = AzulPrincipal,
        surface = FondoBlanco, onSurface = TextoOscuro
    )) {
        Scaffold(
            topBar = { TopAppBar(
                title = { Text(titulo) },
                navigationIcon = { TextButton(onClick = volver) { Text("Volver", color = FondoBlanco) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPrincipal, titleContentColor = FondoBlanco)
            ) },
            containerColor = FondoBlanco
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                .imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), content = contenido)
        }
    }
}

@Composable
private fun SelectorPaciente(pacientes: List<Paciente>, seleccionado: String, enabled: Boolean, onSeleccionar: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    val paciente = pacientes.find { it.id == seleccionado }
    Box {
        OutlinedButton(onClick = { abierto = true }, enabled = enabled && pacientes.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
            Text(paciente?.let { "${it.nombre} ${it.apellidoPaterno} · CI ${it.ci}" } ?: "Seleccionar paciente")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            pacientes.forEach { p -> DropdownMenuItem(
                text = { Text("${p.nombre} ${p.apellidoPaterno} · CI ${p.ci}") },
                onClick = { abierto = false; onSeleccionar(p.id) }
            ) }
        }
    }
}

@Composable
private fun MensajeOperacion(estado: CitasEstado) {
    if (estado.ocupado) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Guardando cambios…") }
    if (estado.error.isNotBlank()) Text(estado.error, color = TextoError)
}

@Composable
fun AgendaCitasScreen(
    datos: AgendaDatos, estado: CitasEstado,
    onRol: (RolDemo) -> Unit, onPaciente: (String) -> Unit,
    onNueva: () -> Unit, onDetalle: (String) -> Unit, onVolver: () -> Unit
) {
    MarcoCitas("Agenda de citas", onVolver) {
        Text("Demostración local · sin inicio de sesión", color = AzulPrincipal)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = estado.rol == RolDemo.MEDICO, onClick = { onRol(RolDemo.MEDICO) }, label = { Text("Médico demo") }, enabled = !estado.ocupado)
            FilterChip(selected = estado.rol == RolDemo.PACIENTE, onClick = { onRol(RolDemo.PACIENTE) }, label = { Text("Paciente demo") }, enabled = !estado.ocupado)
        }
        if (datos.cargando) CircularProgressIndicator()
        if (datos.error.isNotEmpty()) Text(datos.error, color = TextoError)
        MensajeOperacion(estado)
        if (estado.rol == RolDemo.MEDICO) {
            Button(onClick = onNueva, enabled = !estado.ocupado && !datos.cargando && datos.pacientes.isNotEmpty()) { Text("Crear cita") }
            if (!datos.cargando && datos.pacientes.isEmpty()) Text("Primero agrega un paciente desde el inicio.")
        } else {
            Text("Selecciona el paciente que representarás en esta demostración.")
            SelectorPaciente(datos.pacientes, estado.pacienteDemoId, !estado.ocupado, onPaciente)
        }
        val visibles = if (estado.rol == RolDemo.MEDICO) datos.citas else datos.citas.filter { it.pacienteId == estado.pacienteDemoId }
        if (!datos.cargando && datos.error.isEmpty() && visibles.isEmpty()) {
            Text(if (estado.rol == RolDemo.PACIENTE && estado.pacienteDemoId.isEmpty()) "Selecciona un paciente para ver sus propuestas." else "No hay citas para mostrar.")
        }
        visibles.forEach { cita ->
            Card(onClick = { onDetalle(cita.id) }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AzulClaro)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val p = datos.pacientes.find { it.id == cita.pacienteId }
                    Text(p?.let { "${it.nombre} ${it.apellidoPaterno}" } ?: "Paciente", style = MaterialTheme.typography.titleMedium)
                    Text("${cita.fecha} · ${cita.hora}")
                    Text(cita.motivo)
                    Text("Estado: ${cita.estado.etiqueta}")
                    Text("Ver cita", color = AzulOscuro)
                }
            }
        }
    }
}

@Composable
fun FormularioCitaScreen(datos: AgendaDatos, estado: CitasEstado, onCambio: (FormularioCita) -> Unit, onGuardar: () -> Unit, onVolver: () -> Unit) {
    val f = estado.formulario
    MarcoCitas(if (f.id == null) "Nueva cita" else "Editar cita", onVolver) {
        Text("Médico demo · propuesta pendiente", color = AzulPrincipal)
        if (estado.rol != RolDemo.MEDICO) {
            Text("El paciente demo no puede crear ni editar citas.")
        } else {
            SelectorPaciente(datos.pacientes, f.pacienteId, !estado.ocupado) { onCambio(f.copy(pacienteId = it)) }
            OutlinedTextField(value = f.fecha, onValueChange = { onCambio(f.copy(fecha = it)) }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = f.hora, onValueChange = { onCambio(f.copy(hora = it)) }, label = { Text("Hora (HH:mm)") }, singleLine = true, enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = f.motivo, onValueChange = { onCambio(f.copy(motivo = it)) }, label = { Text("Motivo de la cita") }, minLines = 2, enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth())
            MensajeOperacion(estado)
            Button(onClick = onGuardar, enabled = !estado.ocupado) { Text("Guardar cita") }
        }
    }
}

@Composable
fun DetalleCitaScreen(id: String, datos: AgendaDatos, estado: CitasEstado, onEditar: (Cita) -> Unit, onResponder: (EstadoCita) -> Unit, onEliminar: () -> Unit, onVolver: () -> Unit) {
    val cita = datos.citas.find { it.id == id }
    var confirmar by rememberSaveable(id) { mutableStateOf(false) }
    MarcoCitas("Detalle de cita", onVolver) {
        Text("${if (estado.rol == RolDemo.MEDICO) "Médico" else "Paciente"} demo · datos locales")
        if (datos.cargando) CircularProgressIndicator()
        else if (datos.error.isNotEmpty()) Text(datos.error, color = TextoError)
        else if (cita == null) Text("La cita ya no está disponible.")
        else if (estado.rol == RolDemo.PACIENTE && estado.pacienteDemoId != cita.pacienteId) Text("Esta cita no corresponde al paciente seleccionado.")
        else {
            val paciente = datos.pacientes.find { it.id == cita.pacienteId }
            Text(paciente?.let { "${it.nombre} ${it.apellidoPaterno} ${it.apellidoMaterno}" } ?: "Paciente", style = MaterialTheme.typography.titleLarge)
            Text("Fecha: ${cita.fecha}")
            Text("Hora: ${cita.hora}")
            Text("Motivo: ${cita.motivo}")
            Text("Estado: ${cita.estado.etiqueta}")
            MensajeOperacion(estado)
            if (estado.rol == RolDemo.MEDICO) {
                if (cita.estado == EstadoCita.PENDIENTE) Button(onClick = { onEditar(cita) }, enabled = !estado.ocupado) { Text("Editar cita") }
                else Text("Esta propuesta ya fue respondida. Para otro horario, crea una nueva cita.")
                OutlinedButton(onClick = { confirmar = true }, enabled = !estado.ocupado) { Text("Eliminar cita") }
            } else if (cita.estado == EstadoCita.PENDIENTE) {
                Button(onClick = { onResponder(EstadoCita.ACEPTADA) }, enabled = !estado.ocupado) { Text("Aceptar cita") }
                OutlinedButton(onClick = { onResponder(EstadoCita.RECHAZADA) }, enabled = !estado.ocupado) { Text("Rechazar cita") }
            } else Text("Tu respuesta ya fue registrada.")
        }
    }
    if (confirmar && cita != null && estado.rol == RolDemo.MEDICO) AlertDialog(
        onDismissRequest = { confirmar = false }, title = { Text("¿Eliminar esta cita?") },
        text = { Text("Se eliminará la cita de ${cita.fecha} a las ${cita.hora}. Esta acción no se puede deshacer.") },
        confirmButton = { TextButton(onClick = { confirmar = false; onEliminar() }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Conservar") } }
    )
}
