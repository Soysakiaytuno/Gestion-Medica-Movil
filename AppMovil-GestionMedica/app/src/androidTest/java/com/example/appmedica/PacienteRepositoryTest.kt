package com.example.appmedica

import androidx.room.Room
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.FormularioCita
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.repository.CitaRepository
import com.example.appmedica.repository.PacienteRepository
import com.example.appmedica.repository.ResultadoRegistroPaciente
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    @Test
    fun asegurarPacienteParaPerfilCreaRegistroBaseValidoCompatibleConCitasYFecha() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val pacientes = PacienteRepository(db)
            val citas = CitaRepository(db)

            val perfil = PerfilUsuario(
                uid = "u1",
                nombre = "  Ana López  ",
                correo = "  ana@example.com  ",
                rol = RolUsuario.PACIENTE,
                activo = true,
                pacienteId = "pac-auto-01"
            )
            pacientes.asegurarPacienteParaPerfil(perfil)

            val creado = pacientes.observarPaciente("pac-auto-01").first()
            assertNotNull(creado)
            assertEquals("Ana López", creado!!.nombre)
            assertEquals("", creado.apellidoPaterno)
            assertEquals("", creado.apellidoMaterno)
            assertEquals("SIN-CI-pac-auto-01", creado.ci)
            assertEquals("ana@example.com", creado.email)
            assertEquals("", creado.celular)
            assertEquals(emptyList<Diagnostico>(), creado.diagnosticos)
            assertEquals(LocalDate.of(2000, 1, 1), creado.fechaNacimiento)

            // Idempotente en llamadas repetidas con datos modificados.
            pacientes.asegurarPacienteParaPerfil(
                perfil.copy(nombre = "Nombre Distinto", correo = "otro@example.com")
            )
            assertEquals(creado, pacientes.observarPaciente("pac-auto-01").first())

            // Compatible con FK de citas.
            citas.guardar(
                FormularioCita(
                    pacienteId = "pac-auto-01",
                    fecha = LocalDate.now().plusDays(1).toString(),
                    hora = "10:00",
                    motivo = "Primera consulta"
                ),
                RolUsuario.MEDICO
            )
            assertEquals("pac-auto-01", citas.citas.first().single().pacienteId)

            // Respaldo de nombre cuando llega en blanco.
            pacientes.asegurarPacienteParaPerfil(
                PerfilUsuario(
                    uid = "u2",
                    nombre = "   ",
                    correo = "sin-nombre@example.com",
                    rol = RolUsuario.PACIENTE,
                    activo = true,
                    pacienteId = "pac-nombre-blanco"
                )
            )
            assertEquals("Paciente", pacientes.observarPaciente("pac-nombre-blanco").first()!!.nombre)
        } finally {
            db.close()
        }
    }

    @Test
    fun asegurarPacienteParaPerfilConservaPacienteExistenteYDiagnosticos() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val pacientes = PacienteRepository(db)
            val josue = Paciente(
                id = "paciente-josue-01",
                nombre = "Josue",
                apellidoPaterno = "Balbontin",
                apellidoMaterno = "Perez",
                ci = "8493021",
                email = "josue.balbontin@email.com",
                celular = "70012345",
                diagnosticos = listOf(
                    Diagnostico("diagnostico-josue-01", "04/10/2026", "Asma", "Desc 1"),
                    Diagnostico("diagnostico-josue-02", "18/08/2026", "Bronquitis Aguda", "Desc 2")
                ),
                fechaNacimiento = LocalDate.of(1999, 5, 14)
            )
            assertEquals(ResultadoRegistroPaciente.GUARDADO, pacientes.registrar(josue))

            pacientes.asegurarPacienteParaPerfil(
                PerfilUsuario(
                    uid = "uid-josue",
                    nombre = "Otro Nombre",
                    correo = "otro@example.com",
                    rol = RolUsuario.PACIENTE,
                    activo = true,
                    pacienteId = "paciente-josue-01"
                )
            )
            assertEquals(josue, pacientes.observarPaciente("paciente-josue-01").first())

            // Verificación sobre la BD pre-poblada AppDatabase.obtener (sin cerrar ni borrar el singleton).
            val dbSingleton = AppDatabase.obtener(contexto)
            val repoSingleton = PacienteRepository(dbSingleton)
            val antes = repoSingleton.observarPaciente("paciente-josue-01").first()!!
            assertEquals(2, antes.diagnosticos.size)
            repoSingleton.asegurarPacienteParaPerfil(
                PerfilUsuario(
                    uid = "uid-josue-singleton",
                    nombre = "Sobrescritura",
                    correo = "nuevo@example.com",
                    rol = RolUsuario.PACIENTE,
                    activo = true,
                    pacienteId = "paciente-josue-01"
                )
            )
            assertEquals(antes, repoSingleton.observarPaciente("paciente-josue-01").first())
        } finally {
            db.close()
        }
    }

    @Test
    fun asegurarPacienteParaPerfilResuelveColisionesEmailNocaseYCi() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val pacientes = PacienteRepository(db)
            assertEquals(
                ResultadoRegistroPaciente.GUARDADO,
                pacientes.registrar(
                    Paciente(
                        id = "pac-1",
                        nombre = "Uno",
                        apellidoPaterno = "Test",
                        apellidoMaterno = "A",
                        ci = "SIN-CI-pac-col",
                        email = "Repetido@Example.com",
                        celular = "70000001",
                        diagnosticos = emptyList(),
                        fechaNacimiento = LocalDate.of(1995, 1, 1)
                    )
                )
            )
            assertEquals(
                ResultadoRegistroPaciente.GUARDADO,
                pacientes.registrar(
                    Paciente(
                        id = "pac-2",
                        nombre = "Dos",
                        apellidoPaterno = "Test",
                        apellidoMaterno = "B",
                        ci = "SIN-CI-pac-col-1",
                        email = "pac-col@paciente.local",
                        celular = "70000002",
                        diagnosticos = emptyList(),
                        fechaNacimiento = LocalDate.of(1996, 1, 1)
                    )
                )
            )

            pacientes.asegurarPacienteParaPerfil(
                PerfilUsuario(
                    uid = "u-col",
                    nombre = "Carlos",
                    correo = "repetido@example.com",
                    rol = RolUsuario.PACIENTE,
                    activo = true,
                    pacienteId = "pac-col"
                )
            )

            val insertado = pacientes.observarPaciente("pac-col").first()!!
            assertEquals("SIN-CI-pac-col-2", insertado.ci)
            assertEquals("pac-col-1@paciente.local", insertado.email)

            pacientes.asegurarPacienteParaPerfil(
                PerfilUsuario(
                    uid = "u-blank",
                    nombre = "Lucía",
                    correo = "   ",
                    rol = RolUsuario.PACIENTE,
                    activo = true,
                    pacienteId = "pac-blank"
                )
            )
            assertEquals("pac-blank@paciente.local", pacientes.observarPaciente("pac-blank").first()!!.email)
        } finally {
            db.close()
        }
    }

    @Test
    fun asegurarPacienteParaPerfilIgnoraNoPacientesOPacienteIdNuloOVacio() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val pacientes = PacienteRepository(db)
            pacientes.asegurarPacienteParaPerfil(
                PerfilUsuario("m1", "Dr. Demo", "doc@example.com", RolUsuario.MEDICO, true, "med-01")
            )
            pacientes.asegurarPacienteParaPerfil(
                PerfilUsuario("p1", "Pac 1", "p1@example.com", RolUsuario.PACIENTE, true, null)
            )
            pacientes.asegurarPacienteParaPerfil(
                PerfilUsuario("p2", "Pac 2", "p2@example.com", RolUsuario.PACIENTE, true, "")
            )
            pacientes.asegurarPacienteParaPerfil(
                PerfilUsuario("p3", "Pac 3", "p3@example.com", RolUsuario.PACIENTE, true, "   ")
            )
            assertTrue(pacientes.pacientes.first().isEmpty())
        } finally {
            db.close()
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
