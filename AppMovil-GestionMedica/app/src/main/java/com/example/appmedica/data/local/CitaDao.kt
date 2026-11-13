package com.example.appmedica.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appmedica.model.EstadoCita
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Insert
    suspend fun insertar(cita: CitaEntity)

    @Query("SELECT * FROM citas ORDER BY fecha, hora, id")
    fun observarTodas(): Flow<List<CitaEntity>>

    @Query("""
        SELECT * FROM citas
        WHERE (:estado IS NULL OR estado = :estado)
          AND (:pacienteId IS NULL OR pacienteId = :pacienteId)
        ORDER BY fecha, hora, id
    """)
    fun observarFiltradas(estado: EstadoCita?, pacienteId: String?): Flow<List<CitaEntity>>

    @Query("""
        SELECT * FROM citas
        WHERE estado != 'RECHAZADA' AND fecha || ' ' || hora > strftime('%Y-%m-%d %H:%M', 'now', 'localtime')
        ORDER BY fecha, hora, id
        LIMIT 1
    """)
    fun observarProxima(): Flow<CitaEntity?>

    @Query("SELECT * FROM citas WHERE id = :id")
    fun observarPorId(id: String): Flow<CitaEntity?>

    @Query("UPDATE citas SET pacienteId = :pacienteId, fecha = :fecha, hora = :hora, motivo = :motivo WHERE id = :id AND estado = 'PENDIENTE'")
    suspend fun editarPendiente(id: String, pacienteId: String, fecha: String, hora: String, motivo: String): Int

    @Query("UPDATE citas SET estado = :estado WHERE id = :id AND pacienteId = :pacienteId AND estado = 'PENDIENTE'")
    suspend fun responderPendiente(id: String, pacienteId: String, estado: EstadoCita): Int

    @Query("DELETE FROM citas WHERE id = :id")
    suspend fun eliminar(id: String): Int
}
