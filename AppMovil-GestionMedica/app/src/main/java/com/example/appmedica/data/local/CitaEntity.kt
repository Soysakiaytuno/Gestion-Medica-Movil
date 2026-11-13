package com.example.appmedica.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appmedica.model.EstadoCita

@Entity(
    tableName = "citas",
    foreignKeys = [ForeignKey(
        entity = PacienteEntity::class, parentColumns = ["id"], childColumns = ["pacienteId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("pacienteId")]
)
data class CitaEntity(
    @PrimaryKey val id: String,
    val pacienteId: String,
    val fecha: String,
    val hora: String,
    val motivo: String,
    val estado: EstadoCita
)
