package com.example.appmedica.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appmedica.model.Diagnostico

@Database(entities = [PacienteEntity::class], version = 1)
@TypeConverters(DiagnosticosConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pacienteDao(): PacienteDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(contexto: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                contexto.applicationContext,
                AppDatabase::class.java,
                "appmedica.db"
            ).addCallback(PacienteDemostracion)
                .build()
                .also { instancia = it }
        }
    }
}

private object PacienteDemostracion : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val diagnosticos = listOf(
            Diagnostico(
                id = "diagnostico-demo-01",
                fecha = "04/10/2026",
                titulo = "Asma",
                descripcion = "El paciente presenta Tos insistente\n\nla enfermedad es tos\n\nse le receta tal cosa\n\nporque desinflama el pulmon"
            ),
            Diagnostico(
                id = "diagnostico-demo-02",
                fecha = "18/08/2026",
                titulo = "Bronquitis Aguda",
                descripcion = "El paciente acude por dificultad respiratoria leve\n\nse detecta inflamación bronquial\n\nse receta inhalador cada 8 horas"
            )
        )
        db.execSQL(
            """
            INSERT INTO pacientes (
                id, nombre, apellidoPaterno, apellidoMaterno, ci, email, celular, diagnosticos, fechaNacimiento
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf(
                "paciente-josue-01", "Josue", "Balbontin", "Perez", "8493021",
                "josue.balbontin@email.com", "70012345",
                DiagnosticosConverter().guardar(diagnosticos), "1999-05-14"
            )
        )
    }
}
