package com.example.appmedica.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.ui.text.font.FontWeight
import com.example.appmedica.model.*
import com.example.appmedica.ui.components.BotonVolver
import com.example.appmedica.ui.theme.*
import com.example.appmedica.viewmodel.AgendaDatos
import com.example.appmedica.viewmodel.CitasEstado
import com.example.appmedica.viewmodel.DetalleCitaDatos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarcoCitas(
    titulo: String,
    volver: () -> Unit,
    botones: (@Composable ColumnScope.() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    // Tema local que reutiliza la paleta; no modifica las pantallas existentes.
    MaterialTheme(colorScheme = lightColorScheme(
        primary = AzulOscuro, onPrimary = FondoBlanco,
        primaryContainer = AzulClaro, onPrimaryContainer = AzulPrincipal,
        secondaryContainer = AzulPildora, onSecondaryContainer = AzulPrincipal,
        surface = FondoBlanco, onSurface = TextoOscuro
    )) {
        Scaffold(
            topBar = { TopAppBar(
                title = { Text(titulo, fontWeight = FontWeight.Bold) },
                navigationIcon = { BotonVolver(onClick = volver, modifier = Modifier.padding(start = 8.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPrincipal, titleContentColor = FondoBlanco)
            ) },
            containerColor = FondoBlanco
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
                // Contenido: ocupa el espacio disponible y hace scroll
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), content = contenido)
                // Botones: fijos abajo (mismas medidas que en Registrar Paciente)
                if (botones != null) {
                    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 48.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp), content = botones)
                }
            }
        }
    }
}

private fun nombrePaciente(p: Paciente) = "${p.nombre} ${p.apellidoPaterno}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuscadorPaciente(
    pacientes: List<Paciente>,
    coincidencias: List<Paciente>,
    busqueda: String,
    seleccionado: String,
    enabled: Boolean,
    onBusquedaChange: (String) -> Unit,
    onSeleccionar: (String) -> Unit
) {
    val focus = LocalFocusManager.current
    var abierto by remember { mutableStateOf(false) }
    val pacienteSeleccionado = pacientes.find { it.id == seleccionado }
    // Al editar una cita, muestra en el campo el paciente que ya tiene asignado
    LaunchedEffect(pacienteSeleccionado) {
        pacienteSeleccionado?.let { onBusquedaChange(nombrePaciente(it)) }
    }
    ExposedDropdownMenuBox(expanded = abierto && enabled, onExpandedChange = { abierto = it }) {
        OutlinedTextField(
            value = busqueda,
            onValueChange = { texto ->
                onBusquedaChange(texto)
                // Si se escribe sobre un paciente ya elegido, la selección se quita
                if (seleccionado.isNotEmpty()) onSeleccionar("")
                abierto = true
            },
            label = { Text("Paciente") },
            placeholder = { Text("Buscar paciente") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto && enabled) },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = abierto && enabled,
            onDismissRequest = { abierto = false },
            containerColor = AzulClaro
        ) {
            if (coincidencias.isEmpty()) {
                DropdownMenuItem(text = { Text("No se encontraron pacientes") }, onClick = {}, enabled = false)
            }
            coincidencias.forEach { p ->
                DropdownMenuItem(
                    text = { Text("${nombrePaciente(p)} (CI: ${p.ci})") },
                    onClick = {
                        abierto = false
                        focus.clearFocus()
                        onBusquedaChange(nombrePaciente(p))
                        onSeleccionar(p.id)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
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
    onFiltro: (EstadoCita?) -> Unit,
    onNueva: () -> Unit, onDetalle: (String) -> Unit, onVolver: () -> Unit
) {
    MarcoCitas("Tu Agenda de Consultas", onVolver) {
        val consultaActual = datos.consulta == estado.consulta
        val cargando = datos.cargando || !consultaActual
        Button(
            onClick = onNueva,
            enabled = !estado.ocupado && !datos.cargando && datos.pacientes.isNotEmpty(),
            modifier = Modifier.align(Alignment.End)
        ) { Text("Crear cita") }
        if (!cargando && datos.error.isEmpty() && datos.pacientes.isEmpty()) Text("Primero agrega un paciente desde el inicio.")
        FiltrosCita(estado.filtro, onFiltro)
        if (cargando) CircularProgressIndicator()
        if (consultaActual && datos.error.isNotEmpty()) Text(datos.error, color = TextoError)
        MensajeOperacion(estado)
        val visibles = if (consultaActual) datos.citas else emptyList()
        if (!cargando && datos.error.isEmpty() && visibles.isEmpty()) {
            Text(
                if (estado.filtro != null) "No hay citas con este estado." else "No hay citas para mostrar.",
                color = AzulPrincipal
            )
        }
        visibles.forEach { cita ->
            Card(onClick = { onDetalle(cita.id) }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AzulClaro)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val p = datos.pacientes.find { it.id == cita.pacienteId }
                    Text(p?.let { "${it.nombre} ${it.apellidoPaterno}" } ?: "Paciente", style = MaterialTheme.typography.titleMedium)
                    Text("${cita.fecha} · ${cita.hora}")
                    Text(cita.motivo)
                    Text("Estado: ${cita.estado.etiqueta}")
                    Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                        Text("Ver cita", color = AzulOscuro)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = AzulOscuro,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltrosCita(seleccion: EstadoCita?, onFiltro: (EstadoCita?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = seleccion == null, onClick = { onFiltro(null) }, label = { Text("Todas") })
        FilterChip(selected = seleccion == EstadoCita.PENDIENTE, onClick = { onFiltro(EstadoCita.PENDIENTE) }, label = { Text("Pendientes") })
        FilterChip(selected = seleccion == EstadoCita.ACEPTADA, onClick = { onFiltro(EstadoCita.ACEPTADA) }, label = { Text("Aceptadas") })
        FilterChip(selected = seleccion == EstadoCita.RECHAZADA, onClick = { onFiltro(EstadoCita.RECHAZADA) }, label = { Text("Rechazadas") })
    }
}

@Composable
fun FormularioCitaScreen(
    datos: AgendaDatos, estado: CitasEstado,
    busqueda: String, coincidencias: List<Paciente>, onBusquedaChange: (String) -> Unit,
    onCambio: (FormularioCita) -> Unit, onGuardar: () -> Unit, onVolver: () -> Unit
) {
    val f = estado.formulario
    val focus = LocalFocusManager.current
    val esMedico = estado.rol == RolUsuario.MEDICO
    MarcoCitas(
        titulo = if (f.id == null) "Nueva Cita" else "Editar Cita",
        volver = onVolver,
        botones = {
            if (esMedico) {
                Button(
                    onClick = {
                        focus.clearFocus()
                        onGuardar()
                    },
                    enabled = !estado.ocupado,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Guardar Cita") }
                OutlinedButton(
                    onClick = {
                        focus.clearFocus()
                        onVolver()
                    },
                    border = BorderStroke(1.dp, AzulPildora),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = FondoBlanco,
                        contentColor = AzulPrincipal
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancelar", color = AzulPrincipal) }
            }
        }
    ) {
        if (!esMedico) {
            Text("Solo el médico puede crear o editar citas.")
        } else {
            BuscadorPaciente(
                pacientes = datos.pacientes,
                coincidencias = coincidencias,
                busqueda = busqueda,
                seleccionado = f.pacienteId,
                enabled = !estado.ocupado,
                onBusquedaChange = onBusquedaChange,
                onSeleccionar = { onCambio(f.copy(pacienteId = it)) }
            )
            OutlinedTextField(value = f.fecha, onValueChange = { onCambio(f.copy(fecha = it)) }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = f.hora, onValueChange = { onCambio(f.copy(hora = it)) }, label = { Text("Hora (HH:mm)") }, singleLine = true, enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = f.motivo, onValueChange = { onCambio(f.copy(motivo = it)) }, label = { Text("Motivo de la cita") }, minLines = 2, enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth())
            MensajeOperacion(estado)
        }
    }
}

@Composable
fun DetalleCitaScreen(id: String, detalle: DetalleCitaDatos, pacientes: List<Paciente>, estado: CitasEstado, onEditar: (Cita) -> Unit, onResponder: (EstadoCita) -> Unit, onEliminar: () -> Unit, onVolver: () -> Unit) {
    val cita = detalle.cita
    var confirmar by rememberSaveable(id) { mutableStateOf(false) }
    MarcoCitas("Detalle de Cita", onVolver) {
        if (detalle.cargando) CircularProgressIndicator()
        else if (detalle.error.isNotEmpty()) Text(detalle.error, color = TextoError)
        else if (cita == null) Text("La cita ya no está disponible.")
        else if (estado.rol == RolUsuario.PACIENTE && estado.pacienteId != cita.pacienteId) Text("Esta cita no te corresponde.")
        else {
            if (estado.rol == RolUsuario.MEDICO) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    FilledIconButton(
                        onClick = { onEditar(cita) },
                        enabled = !estado.ocupado && cita.estado == EstadoCita.PENDIENTE,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = AzulOscuro, contentColor = FondoBlanco)
                    ) { Icon(Icons.Outlined.Edit, contentDescription = "Editar cita") }
                    FilledIconButton(
                        onClick = { confirmar = true },
                        enabled = !estado.ocupado,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = RojoEliminar, contentColor = FondoBlanco)
                    ) { Icon(Icons.Outlined.Delete, contentDescription = "Eliminar cita") }
                }
            }
            val paciente = pacientes.find { it.id == cita.pacienteId }
            Text(paciente?.let { "${it.nombre} ${it.apellidoPaterno} ${it.apellidoMaterno}" } ?: "Paciente", style = MaterialTheme.typography.titleLarge)
            Text("Fecha: ${cita.fecha}")
            Text("Hora: ${cita.hora}")
            Text("Motivo: ${cita.motivo}")
            Text("Estado: ${cita.estado.etiqueta}")
            MensajeOperacion(estado)
            if (estado.rol == RolUsuario.PACIENTE) {
                if (cita.estado == EstadoCita.PENDIENTE) {
                    Button(onClick = { onResponder(EstadoCita.ACEPTADA) }, enabled = !estado.ocupado) { Text("Aceptar cita") }
                    OutlinedButton(onClick = { onResponder(EstadoCita.RECHAZADA) }, enabled = !estado.ocupado) { Text("Rechazar cita") }
                } else Text("Tu respuesta ya fue registrada.")
            }
        }
        if (confirmar && cita != null && estado.rol == RolUsuario.MEDICO) AlertDialog(
            onDismissRequest = { confirmar = false }, title = { Text("¿Eliminar esta cita?") },
            text = { Text("Se eliminará la cita de ${cita.fecha} a las ${cita.hora}. Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = { confirmar = false; onEliminar() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Conservar") } }
        )
    }
}