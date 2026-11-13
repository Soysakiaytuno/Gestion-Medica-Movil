package com.example.appmedica.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Insert
    suspend fun insertar(paciente: PacienteEntity)

    @Query("SELECT * FROM pacientes ORDER BY rowid")
    fun observarTodos(): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM pacientes WHERE id = :id")
    fun observarPorId(id: String): Flow<PacienteEntity?>

    @Update
    suspend fun actualizar(paciente: PacienteEntity): Int

    @Query("DELETE FROM pacientes WHERE id = :id")
    suspend fun eliminar(id: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM pacientes WHERE ci = :ci)")
    suspend fun existeCi(ci: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM pacientes WHERE email = :email)")
    suspend fun existeEmail(email: String): Boolean
}
