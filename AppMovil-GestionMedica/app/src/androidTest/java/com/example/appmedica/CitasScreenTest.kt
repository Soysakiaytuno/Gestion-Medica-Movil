package com.example.appmedica

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appmedica.model.Cita
import com.example.appmedica.model.EstadoCita
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.ui.screens.AgendaCitasScreen
import com.example.appmedica.ui.screens.DetalleCitaScreen
import com.example.appmedica.viewmodel.AgendaDatos
import com.example.appmedica.viewmodel.CitasEstado
import com.example.appmedica.viewmodel.DetalleCitaDatos
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CitasScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val pacientePrueba = Paciente(
        id = "pac-1",
        nombre = "Josue",
        apellidoPaterno = "Balbontin",
        apellidoMaterno = "Perez",
        ci = "8493021",
        email = "josue@example.com",
        celular = "70012345",
        diagnosticos = emptyList(),
        fechaNacimiento = LocalDate.of(1999, 5, 14)
    )

    @Test
    fun agendaPacienteMuestraMisCitasYOcultaAccionesDeMedico() {
        val estado = CitasEstado(rol = RolUsuario.PACIENTE, pacienteId = "pac-1", filtro = null)
        val datos = AgendaDatos(
            pacientes = emptyList(),
            citas = emptyList(),
            cargando = false,
            consulta = estado.consulta
        )

        compose.setContent {
            AgendaCitasScreen(
                datos = datos,
                estado = estado,
                onFiltro = {},
                onNueva = {},
                onDetalle = {},
                onVolver = {}
            )
        }

        compose.onNodeWithText("Mis Citas").assertIsDisplayed()
        compose.onNodeWithText("No hay citas para mostrar.").assertIsDisplayed()
        compose.onNodeWithText("Tu Agenda de Consultas").assertDoesNotExist()
        compose.onNodeWithText("Crear cita").assertDoesNotExist()
        compose.onNodeWithText("Primero agrega un paciente desde el inicio.").assertDoesNotExist()
    }

    @Test
    fun agendaMedicoSinPacientesDeshabilitaCrearCitaYMuestraAviso() {
        val estado = CitasEstado(rol = RolUsuario.MEDICO)
        val datos = AgendaDatos(
            pacientes = emptyList(),
            citas = emptyList(),
            cargando = false,
            consulta = estado.consulta
        )

        compose.setContent {
            AgendaCitasScreen(
                datos = datos,
                estado = estado,
                onFiltro = {},
                onNueva = {},
                onDetalle = {},
                onVolver = {}
            )
        }

        compose.onNodeWithText("Tu Agenda de Consultas").assertIsDisplayed()
        compose.onNodeWithText("Mis Citas").assertDoesNotExist()
        compose.onNodeWithText("Crear cita")
            .assertIsDisplayed()
            .assertIsNotEnabled()
        compose.onNodeWithText("Primero agrega un paciente desde el inicio.").assertIsDisplayed()
    }

    @Test
    fun agendaMedicoConPacientesHabilitaCrearCitaEInvocaCallback() {
        var nuevaClicks = 0
        val estado = CitasEstado(rol = RolUsuario.MEDICO)
        val datos = AgendaDatos(
            pacientes = listOf(pacientePrueba),
            citas = emptyList(),
            cargando = false,
            consulta = estado.consulta
        )

        compose.setContent {
            AgendaCitasScreen(
                datos = datos,
                estado = estado,
                onFiltro = {},
                onNueva = { nuevaClicks++ },
                onDetalle = {},
                onVolver = {}
            )
        }

        compose.onNodeWithText("Tu Agenda de Consultas").assertIsDisplayed()
        compose.onNodeWithText("Crear cita")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        assertEquals(1, nuevaClicks)
        compose.onNodeWithText("Primero agrega un paciente desde el inicio.").assertDoesNotExist()
    }

    @Test
    fun detalleCitaPacientePendientePermiteAceptarYRechazarSinEditarNiEliminar() {
        val respuestas = mutableListOf<EstadoCita>()
        val cita = Cita(
            id = "c1",
            pacienteId = "pac-1",
            fecha = "2026-12-24",
            hora = "09:30",
            motivo = "Control general",
            estado = EstadoCita.PENDIENTE
        )
        val estado = CitasEstado(rol = RolUsuario.PACIENTE, pacienteId = "pac-1")
        val detalle = DetalleCitaDatos(cita = cita, cargando = false)

        compose.setContent {
            DetalleCitaScreen(
                id = "c1",
                detalle = detalle,
                pacientes = listOf(pacientePrueba),
                estado = estado,
                onEditar = {},
                onResponder = { respuestas.add(it) },
                onEliminar = {},
                onVolver = {}
            )
        }

        compose.onNodeWithText("Detalle de Cita").assertIsDisplayed()
        compose.onNodeWithText("Josue Balbontin Perez").assertIsDisplayed()
        compose.onNodeWithText("24/12/2026").assertIsDisplayed()
        compose.onNodeWithText("09:30").assertIsDisplayed()
        compose.onNodeWithText("Control general").assertIsDisplayed()
        compose.onNodeWithText("Pendiente").assertIsDisplayed()

        compose.onNodeWithContentDescription("Editar cita").assertDoesNotExist()
        compose.onNodeWithContentDescription("Eliminar cita").assertDoesNotExist()

        compose.onNodeWithText("Aceptar cita")
            .assertIsDisplayed()
            .performClick()
        compose.onNodeWithText("Rechazar cita")
            .assertIsDisplayed()
            .performClick()

        assertEquals(listOf(EstadoCita.ACEPTADA, EstadoCita.RECHAZADA), respuestas)
    }

    @Test
    fun detalleCitaDeOtroPacienteMuestraBloqueoYNoPermiteAcciones() {
        val citaAjena = Cita(
            id = "c2",
            pacienteId = "pac-2",
            fecha = "2026-12-24",
            hora = "09:30",
            motivo = "Control general",
            estado = EstadoCita.PENDIENTE
        )
        val estado = CitasEstado(rol = RolUsuario.PACIENTE, pacienteId = "pac-1")
        val detalle = DetalleCitaDatos(cita = citaAjena, cargando = false)

        compose.setContent {
            DetalleCitaScreen(
                id = "c2",
                detalle = detalle,
                pacientes = listOf(pacientePrueba),
                estado = estado,
                onEditar = {},
                onResponder = {},
                onEliminar = {},
                onVolver = {}
            )
        }

        compose.onNodeWithText("Esta cita no te corresponde.").assertIsDisplayed()
        compose.onNodeWithText("Aceptar cita").assertDoesNotExist()
        compose.onNodeWithText("Rechazar cita").assertDoesNotExist()
        compose.onNodeWithText("Control general").assertDoesNotExist()
        compose.onNodeWithContentDescription("Editar cita").assertDoesNotExist()
        compose.onNodeWithContentDescription("Eliminar cita").assertDoesNotExist()
    }

    @Test
    fun medicoNoEditaCitaRespondidaYConfirmaAntesDeEliminar() {
        var eliminaciones = 0
        val cita = Cita("aceptada", "pac-1", "2026-12-24", "09:30", "Control", EstadoCita.ACEPTADA)
        compose.setContent {
            DetalleCitaScreen(cita.id, DetalleCitaDatos(cita, cargando = false), listOf(pacientePrueba),
                CitasEstado(RolUsuario.MEDICO), {}, {}, { eliminaciones++ }, {})
        }
        compose.onNodeWithContentDescription("Editar cita").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Eliminar cita").performClick()
        assertEquals(0, eliminaciones)
        compose.onNodeWithText("Conservar").performClick()
        assertEquals(0, eliminaciones)
        compose.onNodeWithContentDescription("Eliminar cita").performClick()
        compose.onNodeWithText("Eliminar", substring = false).performClick()
        assertEquals(1, eliminaciones)
    }
}
