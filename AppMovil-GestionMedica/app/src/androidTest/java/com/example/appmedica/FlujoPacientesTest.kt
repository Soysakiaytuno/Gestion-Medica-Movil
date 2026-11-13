package com.example.appmedica

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.UUID
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujoPacientesTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun registraBuscaYAbreElHistorialDelPacienteSeleccionado() {
        val sufijo = UUID.randomUUID().toString().take(8)
        val apellido = "Prueba$sufijo"
        val ci = "0" + (sufijo.toLong(16) % 10_000_000).toString().padStart(7, '0')
        val buscar = "Josue $apellido"
        val errorCi = "El CI debe tener 7 u 8 dígitos, sin signos ni espacios internos."
        val errorCelular = "El celular debe tener al menos 8 dígitos, sin signos ni espacios internos."

        compose.onNodeWithText("Buscar paciente").performClick()
        compose.onNodeWithText("Seleccionar paciente").assertIsDisplayed()
        esperarTexto("Nombre: Josue Balbontin")
        compose.onNodeWithText("Nombre: Josue Balbontin").assertIsDisplayed()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(buscar)
        esperarTexto("No se encontraron pacientes")
        compose.onNodeWithText("No se encontraron pacientes").assertIsDisplayed()
        compose.onNodeWithText("Agregar paciente").performClick()
        compose.onNodeWithText("Registrar Paciente").assertIsDisplayed()
        val guardar = compose.onNodeWithText("Guardar Paciente")
        guardar.performScrollTo().performClick()
        compose.onNodeWithText("Todos los campos son obligatorios.").performScrollTo().assertIsDisplayed()

        val campos = compose.onAllNodes(hasSetTextAction())
        listOf("Josue", apellido, "Demo", "+1234567", "2000-02-29", "$sufijo@example.com", "70012345")
            .forEachIndexed { index, valor -> campos[index].performScrollTo().performTextInput(valor) }
        guardar.performScrollTo().performClick()
        compose.onNodeWithText(errorCi).performScrollTo().assertIsDisplayed()

        campos[3].performScrollTo().performTextReplacement(ci)
        campos[6].performScrollTo().performTextReplacement("700 12345")
        guardar.performScrollTo().performClick()
        compose.onNodeWithText(errorCelular).performScrollTo().assertIsDisplayed()

        campos[6].performScrollTo().performTextReplacement("70012345")
        guardar.performScrollTo().performClick()
        esperarTexto("Seleccionar paciente")
        esperarTexto("Nombre: $buscar")
        compose.onNodeWithText("Seleccionar paciente").assertIsDisplayed()
        compose.onNodeWithText("Nombre: $buscar").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Más detalles").assertCountEquals(1)
        compose.onNodeWithText("Más detalles").performClick()
        esperarTexto("CI: $ci")
        compose.onNodeWithText("CI: $ci").assertIsDisplayed()
        compose.onNodeWithText("Este paciente aún no tiene diagnósticos registrados.").assertIsDisplayed()

        compose.onNodeWithText("<").performClick()
        compose.onNodeWithText("Seleccionar paciente").assertIsDisplayed()
        compose.onNodeWithText("<").performClick()
        compose.onNodeWithText("Próxima consulta").assertIsDisplayed()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(buscar)
        closeSoftKeyboard()
        compose.onNodeWithTag("lista_inicio").performScrollToNode(hasText("Nombre: $buscar"))
        esperarTexto("Nombre: $buscar")
        compose.onNodeWithText("Nombre: $buscar").assertIsDisplayed()

        compose.activityRule.scenario.recreate()
        esperarTexto("Próxima consulta")
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement(buscar)
        closeSoftKeyboard()
        compose.onNodeWithTag("lista_inicio").performScrollToNode(hasText("Nombre: $buscar"))
        esperarTexto("Nombre: $buscar")
        compose.onNodeWithText("Nombre: $buscar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Más detalles").performClick()
        esperarTexto("CI: $ci")
        compose.onNodeWithText("CI: $ci").assertIsDisplayed()
    }

    private fun esperarTexto(texto: String) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
