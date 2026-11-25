package com.example.appmedica

import com.example.appmedica.data.remote.*
import com.example.appmedica.model.EstadoCita
import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SupabaseContratoTest {
    @Test fun retrofitEnviaLaSesionFirebaseALaFuncionConElIdNulo() = runBlocking {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            assertEquals("/functions/v1/api-medica/guardar_cita", request.url.encodedPath)
            assertEquals("Bearer token-firebase-normal", request.header("Authorization"))
            val buffer = Buffer()
            request.body!!.writeTo(buffer)
            val body = JsonParser.parseString(buffer.readUtf8()).asJsonObject
            assertTrue(body["p_id"].isJsonNull)
            assertFalse(body.has("rol"))
            assertFalse(body.has("p_perfil"))
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body("""{"pacientes":[],"citas":[]}""".toResponseBody("application/json".toMediaType())).build()
        }.build()
        val api = Retrofit.Builder().baseUrl("https://demo.example/").client(client)
            .addConverterFactory(GsonConverterFactory.create(SupabaseJson.gson)).build().create(SupabaseApi::class.java)
        val datos = api.guardarCita("Bearer token-firebase-normal",
            GuardarCitaPeticion(null, "p", "2030-01-01", "10:00", "Control"))
        assertTrue(datos.citas.isEmpty())
    }

    @Test fun crearCitaIncluyeIdNuloParaResolverLaFirmaRpc() {
        val body = GuardarCitaPeticion(null, "pac-1", "2030-01-01", "10:00", "Control")
        val json = JsonParser.parseString(SupabaseJson.gson.toJson(body)).asJsonObject
        assertTrue(json.has("p_id"))
        assertTrue(json["p_id"].isJsonNull)
        assertEquals("pac-1", json["p_paciente_id"].asString)
    }

    @Test fun respuestaSeMapeaAEntidadesConFechaYHoraDelServidor() {
        val json = """{"pacientes":[{"id":"p","nombre":"Demo","apellido_paterno":"Uno",
            "apellido_materno":"Dos","ci":"12345678","email":"demo@example.com","celular":"70012345",
            "diagnosticos":[],"fecha_nacimiento":"2000-02-29"}],
            "citas":[{"id":"c","paciente_id":"p","fecha":"2030-01-01","hora":"10:00",
            "motivo":"Control","estado":"ACEPTADA"}]}"""
        val datos = SupabaseJson.gson.fromJson(json, DatosMedicosDto::class.java)
        assertEquals("Uno", datos.pacientes.single().toEntity().apellidoPaterno)
        assertEquals("2000-02-29", datos.pacientes.single().toEntity().fechaNacimiento)
        assertEquals("p", datos.citas.single().toEntity().pacienteId)
        assertEquals(EstadoCita.ACEPTADA, datos.citas.single().toEntity().estado)
    }

    @Test fun respuestaDePacienteNoPermiteEnviarPacienteIdNiCamposDeLaCita() {
        val json = JsonParser.parseString(SupabaseJson.gson.toJson(
            ResponderCitaPeticion("c", EstadoCita.RECHAZADA))).asJsonObject
        assertEquals(setOf("p_id", "p_estado"), json.keySet())
        assertEquals("RECHAZADA", json["p_estado"].asString)
    }
}
