package com.example.appmedica.repository

import com.example.appmedica.data.remote.ApiService

data class MedicamentoRemoto(
    val codigoNdc: String,
    val nombreComercial: String,
    val nombreGenerico: String?
)

class CatalogoMedicamentosRepository(private val api: ApiService) {
    suspend fun obtenerMedicamentos(): List<MedicamentoRemoto> =
        api.obtenerMedicamentos().results.orEmpty().mapNotNull { dto ->
            val codigo = dto?.codigoNdc?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val generico = dto.nombreGenerico?.takeIf { it.isNotBlank() }
            val nombre = dto.nombreComercial?.takeIf { it.isNotBlank() } ?: generico
                ?: return@mapNotNull null
            MedicamentoRemoto(codigo, nombre, generico?.takeUnless { it.equals(nombre, ignoreCase = true) })
        }
}
