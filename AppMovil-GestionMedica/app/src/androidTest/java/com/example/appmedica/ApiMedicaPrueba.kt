package com.example.appmedica

import com.example.appmedica.data.remote.*

/** Respuestas controladas para probar el cliente; no sustituye pruebas de RLS en Supabase. */
class ApiMedicaPrueba : SupabaseApi {
    var respuesta = DatosMedicosDto(emptyList(), emptyList())
    var fallo: Exception? = null
    var ultimaPeticion: Any? = null
    var ultimoToken = ""
    var alGuardarPaciente: ((GuardarPacientePeticion) -> DatosMedicosDto)? = null
    private fun responder(token: String, body: Any? = null): DatosMedicosDto {
        ultimoToken = token
        ultimaPeticion = body
        fallo?.let { throw it }
        return respuesta
    }
    override suspend fun datos(token: String, body: Map<String, String>) = responder(token)
    override suspend fun guardarPaciente(token: String, body: GuardarPacientePeticion): DatosMedicosDto {
        alGuardarPaciente?.let { respuesta = it(body) }
        return responder(token, body)
    }
    override suspend fun guardarCita(token: String, body: GuardarCitaPeticion) = responder(token, body)
    override suspend fun responderCita(token: String, body: ResponderCitaPeticion) = responder(token, body)
    override suspend fun eliminarCita(token: String, body: EliminarPeticion) = responder(token, body)
}

fun pacienteRemoto(id: String = "pac-1", ci: String = "12345678") = PacienteDto(
    id, "Paciente", "Demo", "Prueba", ci, "$id@example.com", "70012345", emptyList(), "2000-02-29")
