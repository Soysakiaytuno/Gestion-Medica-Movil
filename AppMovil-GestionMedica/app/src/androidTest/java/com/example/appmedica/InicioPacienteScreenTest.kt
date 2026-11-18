package com.example.appmedica

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.ui.screens.InicioPacienteScreen
import com.example.appmedica.viewmodel.ProximaCita
import com.example.appmedica.viewmodel.ResumenProxima
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InicioPacienteScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun muestraSaludoSubtituloAccesosRapidosYExcluyeAccionesDeMedico() {
        compose.setContent {
            MaterialTheme {
                InicioPacienteScreen(
                    nombre = "Josue",
                    resumenCita = ResumenProxima(cita = null, mensaje = "No tienes próximas citas."),
                    totalDiagnosticos = 0,
                    ultimoDiagnostico = null,
                    irMisCitas = {},
                    irMiHistorial = {},
                    irDetalleCita = {}
                )
            }
        }

        compose.onNodeWithText("¡Hola, Josue!").assertIsDisplayed()
        compose.onNodeWithText("Tu portal de salud").assertIsDisplayed()
        compose.onNodeWithText("Mis citas").assertIsDisplayed()
        compose.onNodeWithText("Mi historial médico").assertIsDisplayed()
        compose.onNodeWithText("Tu próxima cita").assertIsDisplayed()
        compose.onNodeWithText("No tienes próximas citas.")
            .assertIsDisplayed()
            .assertHasNoClickAction()

        compose.onNodeWithTag("lista_inicio_paciente")
            .performScrollToNode(hasText("Ver historial completo"))
        compose.onNodeWithText("Resumen de mi historial").assertIsDisplayed()
        compose.onNodeWithText("Diagnósticos registrados: 0").assertIsDisplayed()
        compose.onNodeWithText("Aún no tienes diagnósticos registrados.").assertIsDisplayed()
        compose.onNodeWithText("Ver historial completo").assertIsDisplayed()

        compose.onNodeWithText("Agregar paciente").assertDoesNotExist()
        compose.onNodeWithText("Recetar").assertDoesNotExist()
        compose.onNodeWithText("Agenda de Citas").assertDoesNotExist()
        compose.onNodeWithText("Buscar nombre").assertDoesNotExist()
    }

    @Test
    fun nombreEnBlancoUsaRespaldoPaciente() {
        compose.setContent {
            MaterialTheme {
                InicioPacienteScreen(
                    nombre = "   ",
                    resumenCita = ResumenProxima(cita = null, mensaje = "No tienes próximas citas."),
                    totalDiagnosticos = 0,
                    ultimoDiagnostico = null,
                    irMisCitas = {},
                    irMiHistorial = {},
                    irDetalleCita = {}
                )
            }
        }

        compose.onNodeWithText("¡Hola, Paciente!").assertIsDisplayed()
    }

    @Test
    fun accesosRapidosYVerHistorialInvocanCallbacks() {
        var citasClicks = 0
        var historialClicks = 0
        compose.setContent {
            MaterialTheme {
                InicioPacienteScreen(
                    nombre = "Josue",
                    resumenCita = ResumenProxima(cita = null, mensaje = "No tienes próximas citas."),
                    totalDiagnosticos = 0,
                    ultimoDiagnostico = null,
                    irMisCitas = { citasClicks++ },
                    irMiHistorial = { historialClicks++ },
                    irDetalleCita = {}
                )
            }
        }

        compose.onNodeWithText("Mis citas").performClick()
        assertEquals(1, citasClicks)

        compose.onNodeWithText("Mi historial médico").performClick()
        assertEquals(1, historialClicks)

        compose.onNodeWithTag("lista_inicio_paciente")
            .performScrollToNode(hasText("Ver historial completo"))
        compose.onNodeWithText("Ver historial completo").performClick()
        assertEquals(2, historialClicks)
    }

    @Test
    fun proximaCitaYUltimoDiagnosticoSeMuestranYAbrenDetalle() {
        var detalleId: String? = null
        compose.setContent {
            MaterialTheme {
                InicioPacienteScreen(
                    nombre = "Josue",
                    resumenCita = ResumenProxima(
                        cita = ProximaCita(
                            citaId = "cita-42",
                            paciente = "Balbontin Josue",
                            motivo = "Control respiratorio",
                            fecha = "2026-12-24",
                            hora = "09:30"
                        ),
                        mensaje = ""
                    ),
                    totalDiagnosticos = 2,
                    ultimoDiagnostico = Diagnostico(
                        id = "d1",
                        fecha = "04/10/2026",
                        titulo = "Asma",
                        descripcion = "Control"
                    ),
                    irMisCitas = {},
                    irMiHistorial = {},
                    irDetalleCita = { detalleId = it }
                )
            }
        }

        compose.onNodeWithText("Control respiratorio").assertIsDisplayed()
        compose.onNodeWithText("24 dic 2026").assertIsDisplayed()
        compose.onNodeWithText("9:30").assertIsDisplayed()
        compose.onNodeWithText("Ver detalles").assertIsDisplayed()
        compose.onNodeWithText("No tienes próximas citas.").assertDoesNotExist()

        compose.onNodeWithText("Control respiratorio").performClick()
        assertEquals("cita-42", detalleId)

        compose.onNodeWithTag("lista_inicio_paciente")
            .performScrollToNode(hasText("Ver historial completo"))
        compose.onNodeWithText("Diagnósticos registrados: 2").assertIsDisplayed()
        compose.onNodeWithText("Último diagnóstico").assertIsDisplayed()
        compose.onNodeWithText("Asma").assertIsDisplayed()
        compose.onNodeWithText("Fecha: 04/10/2026").assertIsDisplayed()
        compose.onNodeWithText("Aún no tienes diagnósticos registrados.").assertDoesNotExist()
    }
}
