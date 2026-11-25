package com.example.appmedica

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.data.remote.DatosMedicosDto
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.navigation.AppNavigation
import com.example.appmedica.repository.*
import com.example.appmedica.ui.screens.RegistrarPacienteScreen
import com.example.appmedica.viewmodel.RegistroPacienteFormulario
import com.example.appmedica.viewmodel.RegistroViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujoPacientesTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var registro: RegistroViewModel
    private lateinit var pacientes: PacienteRepository
    private lateinit var citas: CitaRepository

    @Before fun preparar() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val api = ApiMedicaPrueba()
        api.alGuardarPaciente = { peticion -> DatosMedicosDto(listOf(peticion.p_paciente), emptyList()) }
        val almacen = AlmacenMedico(db, { api }, { "token" })
        runBlocking { almacen.actualizar() }
        pacientes = PacienteRepository(almacen)
        citas = CitaRepository(almacen)
        registro = RegistroViewModel(pacientes)
    }

    @After fun cerrar() { db.close() }

    @Test fun guardaPacienteRemotoYAbreSuHistorialDesdeRoom() {
        // El formulario se precarga para comprobar guardado y navegación, sin
        // depender del teclado del emulador. La validación sigue ejecutándose.
        registro.actualizarFormulario {
            RegistroPacienteFormulario("Josue", "Prueba", "", "01234567",
                "2000-02-29", "demo@example.com", "70012345")
        }
        val perfil = PerfilUsuario("medico-test", "Demo", "medico@example.com", RolUsuario.MEDICO, true)
        compose.setContent {
            MaterialTheme {
                var registrado by remember { mutableStateOf(false) }
                val estado by registro.estado.collectAsStateWithLifecycle()
                if (registrado) {
                    AppNavigation(pacientes, citas, perfil)
                } else {
                    RegistrarPacienteScreen(estado.formulario, estado.mensajeError, estado.guardando,
                        registro::actualizarFormulario, { registro.guardar { registrado = true } }, {})
                }
            }
        }
        compose.onNodeWithText("Guardar Paciente").performClick()
        esperarTexto("Nombre: Josue Prueba")
        compose.onNodeWithTag("lista_inicio").performScrollToNode(hasText("Más detalles"))
        compose.onNodeWithText("Más detalles").performClick()
        esperarTexto("CI: 01234567")
        compose.onNodeWithText("CI: 01234567").assertIsDisplayed()
        compose.onNodeWithText("Este paciente aún no tiene diagnósticos registrados.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithTag("lista_inicio").assertIsDisplayed()
    }

    @Test fun formularioVacioMuestraValidacionYSigueSinPacientes() {
        compose.setContent {
            val estado by registro.estado.collectAsStateWithLifecycle()
            RegistrarPacienteScreen(estado.formulario, estado.mensajeError, estado.guardando,
                registro::actualizarFormulario, { registro.guardar {} }, {})
        }
        compose.onNodeWithText("Guardar Paciente").performClick()
        esperarTexto("Completa todos los campos obligatorios.")
        org.junit.Assert.assertTrue(runBlocking { pacientes.pacientes.first() }.isEmpty())
    }

    private fun esperarTexto(texto: String) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
