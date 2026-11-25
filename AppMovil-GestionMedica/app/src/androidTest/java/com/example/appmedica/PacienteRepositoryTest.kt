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
import java.util.UUID
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PacienteRepositoryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun erroresRemotosNoExponenDetallesTecnicosYConservanLaCopiaLocal() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val api = ApiMedicaPrueba().apply { respuesta = DatosMedicosDto(listOf(pacienteRemoto()), emptyList()) }
            val almacen = AlmacenMedico(db, { api }, { "token" })
            almacen.actualizar()
            val fallos = listOf(
                HttpException(Response.error<Any>(503, "SQL service_role api-medica error interno".toResponseBody())),
                IllegalArgumentException("Configuración SUPABASE_URL inválida"),
                IOException("Error técnico de transporte")
            )
            for (fallo in fallos) {
                api.fallo = fallo
                try {
                    almacen.actualizar()
                    fail("La consulta debía fallar")
                } catch (error: ErrorOperacion) {
                    assertSame(fallo, error.cause)
                    val mensaje = error.message.orEmpty()
                    assertTrue(mensaje.isNotBlank())
                    assertFalse(Regex("HTTP|SQL|Supabase|Firestore|caché|service_role|docs/", RegexOption.IGNORE_CASE).containsMatchIn(mensaje))
                    assertNotEquals(fallo.message, mensaje)
                }
                assertEquals("pac-1", db.pacienteDao().observarTodos().first().single().id)
            }
        } finally { db.close() }
    }

    @Test fun guardaSoloLaRespuestaRemotaYNoInventaUnPacienteAlFallar() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val api = ApiMedicaPrueba()
            val almacen = AlmacenMedico(db, { api }, { "Bearer prueba" })
            val repo = PacienteRepository(almacen)
            val paciente = Paciente(id = "pac-1", nombre = "Nombre enviado", apellidoPaterno = "Demo",
                apellidoMaterno = "Prueba", ci = "12345678", email = "pac-1@example.com",
                celular = "70012345", diagnosticos = emptyList(), fechaNacimiento = LocalDate.of(2000,2,29))
            api.fallo = IOException("sin red")
            try { repo.registrar(paciente); fail("Debe fallar sin conexión") } catch (_: IllegalStateException) {}
            assertTrue(repo.pacientes.first().isEmpty())
            api.fallo = null
            api.respuesta = DatosMedicosDto(listOf(pacienteRemoto()), emptyList())
            assertEquals(ResultadoRegistroPaciente.GUARDADO, repo.registrar(paciente))
            assertEquals("Paciente", repo.pacientes.first().single().nombre)
            assertEquals("Bearer prueba", api.ultimoToken)
            assertEquals("Nombre enviado", (api.ultimaPeticion as GuardarPacientePeticion).p_paciente.nombre)
            assertEquals(ResultadoRegistroPaciente.CI_DUPLICADO,
                repo.registrar(paciente.copy(id = "otro", email = "otro@example.com")))
            assertEquals(ResultadoRegistroPaciente.EMAIL_DUPLICADO,
                repo.registrar(paciente.copy(id = "otro", ci = "87654321", email = "PAC-1@example.com")))
        } finally { db.close() }
    }

    @Test fun cachePersisteYEstaAisladaEntreCuentas() = runBlocking {
        val clave = UUID.randomUUID().toString()
        var db = AppDatabase.paraSesion(context, clave)
        val archivo = checkNotNull(db.openHelper.writableDatabase.path)
        val otra = AppDatabase.paraSesion(context, "$clave-otra")
        val otroArchivo = checkNotNull(otra.openHelper.writableDatabase.path)
        try {
            val api = ApiMedicaPrueba().apply { respuesta = DatosMedicosDto(listOf(pacienteRemoto()), emptyList()) }
            AlmacenMedico(db, { api }, { "token" }).actualizar()
            db.close()
            db = AppDatabase.paraSesion(context, clave)
            assertEquals("pac-1", db.pacienteDao().observarTodos().first().single().id)
            assertTrue(otra.pacienteDao().observarTodos().first().isEmpty())
        } finally {
            db.close(); otra.close()
            context.deleteDatabase(java.io.File(archivo).name)
            context.deleteDatabase(java.io.File(otroArchivo).name)
        }
    }
}
