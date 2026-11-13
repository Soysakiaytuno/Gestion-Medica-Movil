package com.example.appmedica

import androidx.room.Room
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.Paciente
import com.example.appmedica.repository.PacienteRepository
import com.example.appmedica.repository.ResultadoRegistroPaciente
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PacienteRepositoryTest {
    @Test
    fun conservaPacientesAlReabrirYPermiteActualizarYEliminar() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = "pacientes-prueba-${UUID.randomUUID()}.db"
        val paciente = Paciente(
            nombre = "Paciente",
            apellidoPaterno = "Demo",
            apellidoMaterno = "Prueba",
            ci = "01234567",
            email = "demo@example.com",
            celular = "70012345",
            diagnosticos = listOf(
                Diagnostico(fecha = "01/10/2026", titulo = "Demo", descripcion = "Línea 1\nLínea 2")
            ),
            fechaNacimiento = LocalDate.of(2000, 2, 29)
        )
        try {
            conRepositorio(contexto, archivo) { repository ->
                assertEquals(ResultadoRegistroPaciente.GUARDADO, repository.registrar(paciente))
                assertEquals(
                    ResultadoRegistroPaciente.CI_DUPLICADO,
                    repository.registrar(paciente.copy(id = "otro-ci", email = "otro@example.com"))
                )
                assertEquals(
                    ResultadoRegistroPaciente.EMAIL_DUPLICADO,
                    repository.registrar(paciente.copy(id = "otro-email", ci = "12345678", email = "DEMO@example.com"))
                )
            }
            conRepositorio(contexto, archivo) { repository ->
                assertEquals(listOf(paciente), repository.pacientes.first())
                assertEquals(paciente, repository.observarPaciente(paciente.id).first())

                val actualizado = paciente.copy(celular = "70054321")
                assertTrue(repository.actualizar(actualizado))
                assertEquals(actualizado, repository.observarPaciente(paciente.id).first())
                assertTrue(repository.eliminar(paciente.id))
                assertFalse(repository.eliminar(paciente.id))
                assertNull(repository.observarPaciente(paciente.id).first())
                assertTrue(repository.pacientes.first().isEmpty())
            }
        } finally {
            contexto.deleteDatabase(archivo)
        }
    }

    private suspend fun conRepositorio(
        contexto: Context,
        archivo: String,
        probar: suspend (PacienteRepository) -> Unit
    ) {
        val db = Room.databaseBuilder(contexto, AppDatabase::class.java, archivo).build()
        try {
            probar(PacienteRepository(db))
        } finally {
            db.close()
        }
    }
}
