package com.example.appmedica.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Insert
    suspend fun insertarTodos(pacientes: List<PacienteEntity>)

    @Query("DELETE FROM pacientes")
    suspend fun vaciar()

    @Query("SELECT * FROM pacientes ORDER BY rowid")
    fun observarTodos(): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM pacientes WHERE id = :id")
    fun observarPorId(id: String): Flow<PacienteEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM pacientes WHERE ci = :ci)")
    suspend fun existeCi(ci: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM pacientes WHERE email = :email)")
    suspend fun existeEmail(email: String): Boolean
}
