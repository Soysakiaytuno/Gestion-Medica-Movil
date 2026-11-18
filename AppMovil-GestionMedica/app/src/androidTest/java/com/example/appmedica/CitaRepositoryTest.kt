package com.example.appmedica

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.data.local.CitaEntity
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
    private fun paciente(
        id: String = "demo",
        ci: String = "12345678",
        email: String = "demo@example.com"
    ) = Paciente(
        id = id,
        nombre = "Paciente",
        apellidoPaterno = "Demo",
        apellidoMaterno = "Prueba",
        ci = ci,
        email = email,
        celular = "70012345",
        diagnosticos = emptyList(),
        fechaNacimiento = LocalDate.of(2000, 1, 1)
    )

    @Test fun crudRespuestasReactividadYPersistencia() = runBlocking {
        val archivo = "citas-${UUID.randomUUID()}.db"
        var db = abrir(archivo)
        try {
            PacienteRepository(db).registrar(paciente())
            val repo = CitaRepository(db)
            val f = FormularioCita(pacienteId = "demo", fecha = LocalDate.now().plusDays(2).toString(), hora = "10:00", motivo = "Control")
            falla { repo.guardar(f.copy(hora = "25:00"), RolUsuario.MEDICO) }
            falla { repo.guardar(f, RolUsuario.PACIENTE) }
            assertTrue(repo.citas.first().isEmpty())
            repo.guardar(f, RolUsuario.MEDICO)
            val cita = repo.citas.first().single()
            repo.guardar(f.copy(id = cita.id, motivo = "Control editado"), RolUsuario.MEDICO)
            assertEquals("Control editado", repo.observarCita(cita.id).first()!!.motivo)
            falla { repo.responder(cita.id, "otro", EstadoCita.ACEPTADA, RolUsuario.PACIENTE) }
            falla { repo.responder(cita.id, "demo", EstadoCita.ACEPTADA, RolUsuario.MEDICO) }
            falla { repo.responder(cita.id, "demo", EstadoCita.PENDIENTE, RolUsuario.PACIENTE) }
            val emisiones = Channel<List<Cita>>(Channel.UNLIMITED)
            val observador = launch { repo.citas.collect { emisiones.send(it) } }
            try {
                withTimeout(5_000) { assertEquals(EstadoCita.PENDIENTE, emisiones.receive().single().estado) }
                repo.responder(cita.id, "demo", EstadoCita.ACEPTADA, RolUsuario.PACIENTE)
                withTimeout(5_000) { assertEquals(EstadoCita.ACEPTADA, emisiones.receive().single().estado) }
            } finally { observador.cancel() }
            falla { repo.responder(cita.id, "demo", EstadoCita.RECHAZADA, RolUsuario.PACIENTE) }
            falla { repo.guardar(f.copy(id = cita.id), RolUsuario.MEDICO) }
            falla { repo.eliminar(cita.id, RolUsuario.PACIENTE) }
            repo.guardar(f.copy(motivo = "Segunda"), RolUsuario.MEDICO)
            val segunda = repo.citas.first().first { it.id != cita.id }
            repo.responder(segunda.id, "demo", EstadoCita.RECHAZADA, RolUsuario.PACIENTE)
            db.close()
            db = abrir(archivo)
            val reabierto = CitaRepository(db)
            assertEquals(EstadoCita.ACEPTADA, reabierto.observarCita(cita.id).first()!!.estado)
            assertEquals(EstadoCita.RECHAZADA, reabierto.observarCita(segunda.id).first()!!.estado)
            reabierto.eliminar(cita.id, RolUsuario.MEDICO)
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
                repo.guardar(FormularioCita(pacienteId = previo.id, fecha = LocalDate.now().plusDays(1).toString(), hora = "10:00", motivo = "Demo"), RolUsuario.MEDICO)
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
            falla { citas.guardar(formulario.copy(id = idInexistente), RolUsuario.MEDICO) }
            falla { citas.responder(idInexistente, "demo", EstadoCita.ACEPTADA, RolUsuario.PACIENTE) }
            falla { citas.eliminar(idInexistente, RolUsuario.MEDICO) }

            try {
                citas.guardar(formulario, RolUsuario.MEDICO)
                fail("Room debía rechazar una cita con paciente inexistente")
            } catch (_: SQLiteConstraintException) {
                assertTrue(citas.citas.first().isEmpty())
            }

            pacientes.registrar(paciente())
            citas.guardar(formulario.copy(pacienteId = "demo"), RolUsuario.MEDICO)
            val cita = citas.citas.first().single()
            try {
                pacientes.eliminar("demo")
                fail("Room debía conservar el paciente referenciado")
            } catch (_: SQLiteConstraintException) {
                assertNotNull(pacientes.observarPaciente("demo").first())
                assertEquals(cita, citas.observarCita(cita.id).first())
            }
            citas.eliminar(cita.id, RolUsuario.MEDICO)
            assertTrue(pacientes.eliminar("demo"))
        } finally {
            db.close()
        }
    }

    @Test fun observarProximaSinFiltroDevuelveLaMasTempranaNoRechazadaYExcluyePasadasYRechazadas() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val pacientes = PacienteRepository(db)
            val citas = CitaRepository(db)
            pacientes.registrar(paciente(id = "pac-a", ci = "10000001", email = "paca@example.com"))
            pacientes.registrar(paciente(id = "pac-b", ci = "10000002", email = "pacb@example.com"))

            val ayer = LocalDate.now().minusDays(1).toString()
            val manana = LocalDate.now().plusDays(1).toString()
            val pasadoManana = LocalDate.now().plusDays(2).toString()

            // Citas pasadas y cita futura pre-rechazada insertadas directo al DAO.
            db.citaDao().insertar(CitaEntity("pasada-pend", "pac-a", ayer, "10:00", "Pasada pendiente", EstadoCita.PENDIENTE))
            db.citaDao().insertar(CitaEntity("pasada-acep", "pac-b", ayer, "11:00", "Pasada aceptada", EstadoCita.ACEPTADA))
            db.citaDao().insertar(CitaEntity("futura-rech", "pac-a", manana, "08:00", "Futura rechazada", EstadoCita.RECHAZADA))

            citas.guardar(FormularioCita(pacienteId = "pac-a", fecha = manana, hora = "09:00", motivo = "Control A temprano"), RolUsuario.MEDICO)
            citas.guardar(FormularioCita(pacienteId = "pac-b", fecha = manana, hora = "10:00", motivo = "Control B siguiente"), RolUsuario.MEDICO)
            citas.guardar(FormularioCita(pacienteId = "pac-a", fecha = pasadoManana, hora = "11:00", motivo = "Control A posterior"), RolUsuario.MEDICO)

            val todas = citas.citas.first()
            val citaATemprana = todas.first { it.motivo == "Control A temprano" }
            val citaBSiguiente = todas.first { it.motivo == "Control B siguiente" }
            citas.responder(citaBSiguiente.id, "pac-b", EstadoCita.ACEPTADA, RolUsuario.PACIENTE)

            val emisiones = Channel<Cita?>(Channel.UNLIMITED)
            val observador = launch { citas.observarProxima(pacienteId = null).collect { emisiones.send(it) } }
            try {
                val primera = withTimeout(5_000) { emisiones.receive() }
                assertEquals(citaATemprana.id, primera?.id)
                assertEquals("Control A temprano", primera?.motivo)

                // Al rechazar la cita más temprana, el flujo reactivo avanza a la siguiente válida.
                citas.responder(citaATemprana.id, "pac-a", EstadoCita.RECHAZADA, RolUsuario.PACIENTE)
                var siguiente = withTimeout(5_000) { emisiones.receive() }
                while (siguiente?.id == citaATemprana.id) {
                    siguiente = withTimeout(5_000) { emisiones.receive() }
                }
                assertEquals(citaBSiguiente.id, siguiente?.id)
                assertEquals("pac-b", siguiente?.pacienteId)
                assertEquals("Control B siguiente", siguiente?.motivo)
            } finally {
                observador.cancel()
            }
        } finally {
            db.close()
        }
    }

    @Test fun observarProximaPorPacienteAislaResultadosYDevuelveNullSinProximasValidas() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val pacientes = PacienteRepository(db)
            val citas = CitaRepository(db)
            pacientes.registrar(paciente(id = "pac-a", ci = "20000001", email = "a@example.com"))
            pacientes.registrar(paciente(id = "pac-b", ci = "20000002", email = "b@example.com"))

            val ayer = LocalDate.now().minusDays(1).toString()
            val manana = LocalDate.now().plusDays(1).toString()

            // Cita de pac-b más temprana que cualquier cita de pac-a.
            citas.guardar(FormularioCita(pacienteId = "pac-b", fecha = manana, hora = "08:30", motivo = "Control B temprano"), RolUsuario.MEDICO)
            val citaB = citas.citas.first().first { it.pacienteId == "pac-b" }
            citas.responder(citaB.id, "pac-b", EstadoCita.ACEPTADA, RolUsuario.PACIENTE)

            // Citas de pac-a: pasada aceptada, futura rechazada y futura pendiente posterior.
            db.citaDao().insertar(CitaEntity("a-pasada", "pac-a", ayer, "10:00", "Pasada A", EstadoCita.ACEPTADA))
            db.citaDao().insertar(CitaEntity("a-rechazada", "pac-a", manana, "09:00", "Rechazada A", EstadoCita.RECHAZADA))
            citas.guardar(FormularioCita(pacienteId = "pac-a", fecha = manana, hora = "11:00", motivo = "Control A"), RolUsuario.MEDICO)
            val controlA = citas.citas.first().first { it.motivo == "Control A" }

            val proximaA = citas.observarProxima("pac-a").first()
            assertEquals(controlA.id, proximaA?.id)
            assertEquals("pac-a", proximaA?.pacienteId)
            assertEquals("Control A", proximaA?.motivo)

            // Fail-closed para pacienteId en blanco.
            assertNull(citas.observarProxima("").first())

            // Tras rechazar la única cita futura válida de pac-a, su próxima es null aunque pac-b tenga cita futura.
            citas.responder(controlA.id, "pac-a", EstadoCita.RECHAZADA, RolUsuario.PACIENTE)
            assertNull(citas.observarProxima("pac-a").first())
            assertEquals("pac-b", citas.observarProxima(pacienteId = null).first()?.pacienteId)
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
