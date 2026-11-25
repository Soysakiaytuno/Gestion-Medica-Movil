package com.example.appmedica.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PacienteEntity::class, CitaEntity::class], version = 2)
@TypeConverters(DiagnosticosConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pacienteDao(): PacienteDao
    abstract fun citaDao(): CitaDao

    companion object {
        // Caché aislada por cuenta, rol y paciente vinculado. La base local anterior
        // se conserva sin subirla automáticamente ni mezclarla con el servidor.
        fun paraSesion(contexto: Context, claveSesion: String): AppDatabase {
            val hash = java.security.MessageDigest.getInstance("SHA-256")
                .digest(claveSesion.toByteArray()).joinToString("") { "%02x".format(it) }
            return Room.databaseBuilder(contexto.applicationContext, AppDatabase::class.java,
                "cache-medica-$hash.db").build()
        }
    }
}
