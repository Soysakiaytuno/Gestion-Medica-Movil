package com.example.appmedica.data.remote

import com.example.appmedica.data.local.CitaEntity
import com.example.appmedica.data.local.PacienteEntity
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.EstadoCita
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class PacienteDto(
    val id: String,
    val nombre: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String,
    val ci: String,
    val email: String,
    val celular: String,
    val diagnosticos: List<Diagnostico>,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String
) {
    fun toEntity() = PacienteEntity(id, nombre, apellidoPaterno, apellidoMaterno, ci, email,
        celular, diagnosticos, fechaNacimiento)
}

data class CitaDto(
    val id: String,
    @SerializedName("paciente_id") val pacienteId: String,
    val fecha: String,
    val hora: String,
    val motivo: String,
    val estado: EstadoCita
) {
    fun toEntity() = CitaEntity(id, pacienteId, fecha, hora, motivo, estado)
}

// Cada RPC devuelve una instantánea consistente y autorizada para la sesión.
data class DatosMedicosDto(val pacientes: List<PacienteDto>, val citas: List<CitaDto>)
data class GuardarPacientePeticion(val p_paciente: PacienteDto)
data class EliminarPeticion(val p_id: String)
data class GuardarCitaPeticion(val p_id: String?, val p_paciente_id: String,
    val p_fecha: String, val p_hora: String, val p_motivo: String)
data class ResponderCitaPeticion(val p_id: String, val p_estado: EstadoCita)

interface SupabaseApi {
    @POST("functions/v1/api-medica/datos_medicos")
    suspend fun datos(@Header("Authorization") token: String, @Body body: Map<String, String> = emptyMap()): DatosMedicosDto

    @POST("functions/v1/api-medica/guardar_paciente")
    suspend fun guardarPaciente(@Header("Authorization") token: String, @Body body: GuardarPacientePeticion): DatosMedicosDto

    @POST("functions/v1/api-medica/guardar_cita")
    suspend fun guardarCita(@Header("Authorization") token: String, @Body body: GuardarCitaPeticion): DatosMedicosDto

    @POST("functions/v1/api-medica/responder_cita")
    suspend fun responderCita(@Header("Authorization") token: String, @Body body: ResponderCitaPeticion): DatosMedicosDto

    @POST("functions/v1/api-medica/eliminar_cita")
    suspend fun eliminarCita(@Header("Authorization") token: String, @Body body: EliminarPeticion): DatosMedicosDto
}
