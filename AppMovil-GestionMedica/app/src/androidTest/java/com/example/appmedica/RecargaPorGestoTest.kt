package com.example.appmedica

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appmedica.ui.components.ContenidoActualizable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecargaPorGestoTest {
    @get:Rule val compose = createComposeRule()

    @Test fun deslizarDesdeElInicioRecargaUnaVezYNoDuplicaSolicitudes() {
        var solicitudes = 0
        var actualizando by mutableStateOf(false)
        compose.setContent {
            ContenidoActualizable(actualizando, { solicitudes++; actualizando = true }) {
                LazyColumn(Modifier.fillMaxSize()) { item { Text("Datos de la cuenta") } }
            }
        }
        compose.onNodeWithTag("contenido_actualizable").performTouchInput { swipeDown() }
        compose.runOnIdle { assertEquals(1, solicitudes) }
        compose.onNodeWithTag("contenido_actualizable").performTouchInput { swipeDown() }
        compose.runOnIdle { assertEquals(1, solicitudes); actualizando = false }
        compose.onNodeWithText("Actualizar").assertDoesNotExist()
    }

    @Test fun desplazarLaListaLejosDelInicioNoRecarga() {
        var solicitudes = 0
        compose.setContent {
            ContenidoActualizable(false, { solicitudes++ }) {
                LazyColumn(Modifier.fillMaxSize().testTag("lista_prueba")) {
                    items(100) { Text("Paciente $it", Modifier.height(64.dp)) }
                }
            }
        }
        compose.onNodeWithTag("lista_prueba").performScrollToIndex(50)
        compose.onNodeWithTag("contenido_actualizable").performTouchInput { swipeDown() }
        compose.runOnIdle { assertEquals(0, solicitudes) }
    }
}
