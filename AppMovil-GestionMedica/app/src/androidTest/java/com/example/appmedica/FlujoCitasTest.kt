package com.example.appmedica

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import java.util.UUID
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujoCitasTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun medicoCreaEditaPacienteRespondeYMedicoElimina() {
        val motivo = "Control demo ${UUID.randomUUID().toString().take(8)}"
        compose.onNodeWithText("Agenda de citas · Médico / Paciente demo").performScrollTo().performClick()
        esperar("Crear cita")
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("Crear cita") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Crear cita").performClick()
        esperar("Nueva cita")
        compose.onNodeWithText("Guardar cita").performScrollTo().performClick()
        compose.onNodeWithText("Selecciona un paciente y completa todos los campos.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Seleccionar paciente").performScrollTo().performClick()
        esperar("Josue Balbontin · CI 8493021")
        compose.onNodeWithText("Josue Balbontin · CI 8493021").performClick()
        compose.onNodeWithText("Fecha (AAAA-MM-DD)").performScrollTo().performTextInput(LocalDate.now().plusDays(2).toString())
        compose.onNodeWithText("Hora (HH:mm)").performScrollTo().performTextInput("10:30")
        compose.onNodeWithText("Motivo de la cita").performScrollTo().performTextInput(motivo)
        compose.onNodeWithText("Guardar cita").performScrollTo().performClick()
        esperar(motivo)
        compose.onNodeWithText(motivo).performScrollTo().performClick()
        esperar("Detalle de cita")
        compose.onNodeWithText("Editar cita").performScrollTo().performClick()
        compose.onNodeWithText("Motivo de la cita").performScrollTo().performTextReplacement("$motivo editado")
        compose.onNodeWithText("Guardar cita").performScrollTo().performClick()
        esperar("Motivo: $motivo editado")
        compose.onNodeWithText("Volver").performClick()
        compose.onNodeWithText("Paciente demo").performScrollTo().performClick()
        compose.onNodeWithText("Crear cita").assertDoesNotExist()
        compose.onNodeWithText("Seleccionar paciente").performScrollTo().performClick()
        compose.onNodeWithText("Josue Balbontin · CI 8493021").performClick()
        compose.onNodeWithText("$motivo editado").performScrollTo().performClick()
        esperar("Aceptar cita")
        compose.onNodeWithText("Editar cita").assertDoesNotExist()
        compose.onNodeWithText("Eliminar cita").assertDoesNotExist()
        compose.onNodeWithText("Aceptar cita").performScrollTo().performClick()
        esperar("Estado: Aceptada")
        compose.onNodeWithText("Aceptar cita").assertDoesNotExist()
        compose.onNodeWithText("Rechazar cita").assertDoesNotExist()
        compose.activityRule.scenario.recreate()
        esperar("Estado: Aceptada")
        compose.onNodeWithText("Volver").performClick()
        compose.onNodeWithText("Médico demo").performScrollTo().performClick()
        compose.onNodeWithText("$motivo editado").performScrollTo().performClick()
        compose.onNodeWithText("Eliminar cita").performScrollTo().performClick()
        compose.onNodeWithText("Conservar").performClick()
        compose.onNodeWithText("Estado: Aceptada").assertIsDisplayed()
        compose.onNodeWithText("Eliminar cita").performScrollTo().performClick()
        compose.onNodeWithText("Eliminar").performClick()
        esperar("Agenda de citas")
        compose.onNodeWithText("$motivo editado").assertDoesNotExist()
    }

    private fun esperar(texto: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
    }
}
