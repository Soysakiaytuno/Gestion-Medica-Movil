package com.example.appmedica

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.model.*
import com.example.appmedica.repository.CitaRepository
import com.example.appmedica.repository.PacienteRepository
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CitaRepositoryTest {
    private val contexto get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun abrir(archivo: String) = Room.databaseBuilder(contexto, AppDatabase::class.java, archivo)
        .addMigrations(AppDatabase.MIGRATION_1_2).build()
    private fun paciente() = Paciente(id = "demo", nombre = "Paciente", apellidoPaterno = "Demo",
        apellidoMaterno = "Prueba", ci = "12345678", email = "demo@example.com", celular = "70012345",
        diagnosticos = emptyList(), fechaNacimiento = LocalDate.of(2000, 1, 1))

    @Test fun crudRespuestasReactividadYPersistencia() = runBlocking {
        val archivo = "citas-${UUID.randomUUID()}.db"
        var db = abrir(archivo)
        try {
            PacienteRepository(db).registrar(paciente())
            val repo = CitaRepository(db)
            val f = FormularioCita(pacienteId = "demo", fecha = LocalDate.now().plusDays(2).toString(), hora = "10:00", motivo = "Control")
            falla { repo.guardar(f.copy(hora = "25:00"), RolDemo.MEDICO) }
            falla { repo.guardar(f, RolDemo.PACIENTE) }
            assertTrue(repo.citas.first().isEmpty())
            repo.guardar(f, RolDemo.MEDICO)
            val cita = repo.citas.first().single()
            repo.guardar(f.copy(id = cita.id, motivo = "Control editado"), RolDemo.MEDICO)
            assertEquals("Control editado", repo.observarCita(cita.id).first()!!.motivo)
            falla { repo.responder(cita.id, "otro", EstadoCita.ACEPTADA, RolDemo.PACIENTE) }
            falla { repo.responder(cita.id, "demo", EstadoCita.ACEPTADA, RolDemo.MEDICO) }
            falla { repo.responder(cita.id, "demo", EstadoCita.PENDIENTE, RolDemo.PACIENTE) }
            val emisiones = Channel<List<Cita>>(Channel.UNLIMITED)
            val observador = launch { repo.citas.collect { emisiones.send(it) } }
            try {
                withTimeout(5_000) { assertEquals(EstadoCita.PENDIENTE, emisiones.receive().single().estado) }
                repo.responder(cita.id, "demo", EstadoCita.ACEPTADA, RolDemo.PACIENTE)
                withTimeout(5_000) { assertEquals(EstadoCita.ACEPTADA, emisiones.receive().single().estado) }
            } finally { observador.cancel() }
            falla { repo.responder(cita.id, "demo", EstadoCita.RECHAZADA, RolDemo.PACIENTE) }
            falla { repo.guardar(f.copy(id = cita.id), RolDemo.MEDICO) }
            falla { repo.eliminar(cita.id, RolDemo.PACIENTE) }
            repo.guardar(f.copy(motivo = "Segunda"), RolDemo.MEDICO)
            val segunda = repo.citas.first().first { it.id != cita.id }
            repo.responder(segunda.id, "demo", EstadoCita.RECHAZADA, RolDemo.PACIENTE)
            db.close()
            db = abrir(archivo)
            val reabierto = CitaRepository(db)
            assertEquals(EstadoCita.ACEPTADA, reabierto.observarCita(cita.id).first()!!.estado)
            assertEquals(EstadoCita.RECHAZADA, reabierto.observarCita(segunda.id).first()!!.estado)
            reabierto.eliminar(cita.id, RolDemo.MEDICO)
            assertNull(reabierto.observarCita(cita.id).first())
            assertEquals(1, reabierto.citas.first().size)
        } finally { db.close(); contexto.deleteDatabase(archivo) }
    }

    @Test fun migracionDeVersionUnoConservaPacienteYDiagnosticos() = runBlocking {
        val archivo = "migracion-${UUID.randomUUID()}.db"
        try {
            // Esquema original v1: no se usa fallbackToDestructiveMigration.
            contexto.openOrCreateDatabase(archivo, 0, null).use { db ->
                db.execSQL("CREATE TABLE pacientes (id TEXT NOT NULL PRIMARY KEY, nombre TEXT NOT NULL, apellidoPaterno TEXT NOT NULL, apellidoMaterno TEXT NOT NULL, ci TEXT NOT NULL, email TEXT NOT NULL COLLATE NOCASE, celular TEXT NOT NULL, diagnosticos TEXT NOT NULL, fechaNacimiento TEXT NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX index_pacientes_ci ON pacientes(ci)")
                db.execSQL("CREATE UNIQUE INDEX index_pacientes_email ON pacientes(email)")
                db.execSQL("INSERT INTO pacientes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", arrayOf(
                    "previo", "Paciente", "Anterior", "Demo", "87654321", "anterior@example.com", "70012345",
                    "[{\"id\":\"diag\",\"fecha\":\"01/01/2026\",\"titulo\":\"Demo\",\"descripcion\":\"Conservar\"}]", "2000-01-01"))
                db.version = 1
            }
            val db = abrir(archivo)
            try {
                val previo = PacienteRepository(db).observarPaciente("previo").first()!!
                assertEquals("Anterior", previo.apellidoPaterno)
                assertEquals("Conservar", previo.diagnosticos.single().descripcion)
                val repo = CitaRepository(db)
                repo.guardar(FormularioCita(pacienteId = previo.id, fecha = LocalDate.now().plusDays(1).toString(), hora = "10:00", motivo = "Demo"), RolDemo.MEDICO)
                assertEquals(previo.id, repo.citas.first().single().pacienteId)
            } finally { db.close() }
        } finally { contexto.deleteDatabase(archivo) }
    }

    @Test fun conservaLaRelacionPacienteCitaYRechazaOperacionesSobreIdsInexistentes() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val pacientes = PacienteRepository(db)
            val citas = CitaRepository(db)
            val idInexistente = "inexistente"
            val formulario = FormularioCita(
                pacienteId = idInexistente,
                fecha = LocalDate.now().plusDays(2).toString(),
                hora = "10:00",
                motivo = "Integridad de Room"
            )
            assertNull(pacientes.observarPaciente(idInexistente).first())
            assertNull(citas.observarCita(idInexistente).first())
            falla { citas.guardar(formulario.copy(id = idInexistente), RolDemo.MEDICO) }
            falla { citas.responder(idInexistente, "demo", EstadoCita.ACEPTADA, RolDemo.PACIENTE) }
            falla { citas.eliminar(idInexistente, RolDemo.MEDICO) }

            try {
                citas.guardar(formulario, RolDemo.MEDICO)
                fail("Room debía rechazar una cita con paciente inexistente")
            } catch (_: SQLiteConstraintException) {
                assertTrue(citas.citas.first().isEmpty())
            }

            pacientes.registrar(paciente())
            citas.guardar(formulario.copy(pacienteId = "demo"), RolDemo.MEDICO)
            val cita = citas.citas.first().single()
            try {
                pacientes.eliminar("demo")
                fail("Room debía conservar el paciente referenciado")
            } catch (_: SQLiteConstraintException) {
                assertNotNull(pacientes.observarPaciente("demo").first())
                assertEquals(cita, citas.observarCita(cita.id).first())
            }
            citas.eliminar(cita.id, RolDemo.MEDICO)
            assertTrue(pacientes.eliminar("demo"))
        } finally {
            db.close()
        }
    }

    private suspend fun falla(accion: suspend () -> Unit) {
        try { accion(); fail("La operación debía rechazarse") }
        catch (_: IllegalArgumentException) { }
        catch (_: IllegalStateException) { }
    }
}
