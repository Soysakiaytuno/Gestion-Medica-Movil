package com.example.appmedica

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.data.remote.*
import com.example.appmedica.model.*
import com.example.appmedica.repository.*
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CitaRepositoryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun flujoRemotoFiltrosRespuestaYEliminacion() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val api = ApiMedicaPrueba()
            val almacen = AlmacenMedico(db, { api }, { "token" })
            val repo = CitaRepository(almacen)
            val manana = LocalDate.now().plusDays(1).toString()
            val cita = CitaDto("c1", "pac-1", manana, "10:00", "Control", EstadoCita.PENDIENTE)
            api.respuesta = DatosMedicosDto(listOf(pacienteRemoto()), listOf(cita))
            repo.guardar(FormularioCita(pacienteId = "pac-1", fecha = manana, hora = "10:00", motivo = "Control"), RolUsuario.MEDICO)
            assertTrue(api.ultimaPeticion is GuardarCitaPeticion)
            assertEquals("c1", repo.observarProxima("pac-1").first()?.id)
            assertNull(repo.observarProxima("otro").first())
            api.fallo = IOException("sin conexión")
            try { repo.eliminar("c1", RolUsuario.MEDICO); fail("Debe fallar") } catch (_: IllegalStateException) {}
            assertEquals("c1", repo.observarAgenda(null, null).first().single().id)
            api.fallo = null
            api.respuesta = api.respuesta.copy(citas = listOf(cita.copy(estado = EstadoCita.ACEPTADA)))
            repo.responder("c1", EstadoCita.ACEPTADA, RolUsuario.PACIENTE)
            assertEquals(ResponderCitaPeticion("c1", EstadoCita.ACEPTADA), api.ultimaPeticion)
            assertTrue(repo.observarAgenda(EstadoCita.PENDIENTE, "pac-1").first().isEmpty())
            assertEquals(1, repo.observarAgenda(EstadoCita.ACEPTADA, "pac-1").first().size)
            api.respuesta = api.respuesta.copy(citas = emptyList())
            repo.eliminar("c1", RolUsuario.MEDICO)
            assertNull(repo.observarCita("c1").first())
        } finally { db.close() }
    }

    @Test fun refrescoEsAtomicoYConservaLaCopiaAnteriorSiElServidorFalla() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val api = ApiMedicaPrueba().apply { respuesta = DatosMedicosDto(listOf(pacienteRemoto()), emptyList()) }
            val almacen = AlmacenMedico(db, { api }, { "token" })
            almacen.actualizar()
            // Respuesta inconsistente: la FK debe revertir toda la sustitución de caché.
            api.respuesta = DatosMedicosDto(emptyList(), listOf(CitaDto("c1", "ausente", "2030-01-01", "10:00", "Demo", EstadoCita.PENDIENTE)))
            try { almacen.actualizar(); fail("Debe rechazar la FK") } catch (_: IllegalStateException) {}
            assertEquals("pac-1", db.pacienteDao().observarTodos().first().single().id)
            assertTrue(db.citaDao().observarFiltradas(null, null).first().isEmpty())
            api.respuesta = DatosMedicosDto(emptyList(), emptyList())
            almacen.actualizar()
            assertTrue(db.pacienteDao().observarTodos().first().isEmpty())
        } finally { db.close() }
    }
}
