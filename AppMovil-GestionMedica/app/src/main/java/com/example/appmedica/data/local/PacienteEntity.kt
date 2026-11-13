package com.example.appmedica.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.appmedica.model.Diagnostico
import org.json.JSONArray
import org.json.JSONObject

@Entity(
    tableName = "pacientes",
    indices = [Index(value = ["ci"], unique = true), Index(value = ["email"], unique = true)]
)
data class PacienteEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val ci: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val email: String,
    val celular: String,
    val diagnosticos: List<Diagnostico>,
    val fechaNacimiento: String
)

class DiagnosticosConverter {
    @TypeConverter
    fun guardar(diagnosticos: List<Diagnostico>): String {
        val json = JSONArray()
        diagnosticos.forEach { diagnostico ->
            json.put(
                JSONObject()
                    .put("id", diagnostico.id)
                    .put("fecha", diagnostico.fecha)
                    .put("titulo", diagnostico.titulo)
                    .put("descripcion", diagnostico.descripcion)
            )
        }
        return json.toString()
    }

    @TypeConverter
    fun leer(valor: String): List<Diagnostico> {
        val json = JSONArray(valor)
        return List(json.length()) { index ->
            val item = json.getJSONObject(index)
            Diagnostico(
                id = item.getString("id"),
                fecha = item.getString("fecha"),
                titulo = item.getString("titulo"),
                descripcion = item.getString("descripcion")
            )
        }
    }
}
