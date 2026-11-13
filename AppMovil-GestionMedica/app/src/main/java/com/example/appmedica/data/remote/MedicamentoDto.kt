package com.example.appmedica.data.remote

import com.google.gson.annotations.SerializedName

data class CatalogoMedicamentosDto(val results: List<MedicamentoDto?>?)

data class MedicamentoDto(
    @SerializedName("product_ndc") val codigoNdc: String?,
    @SerializedName("brand_name") val nombreComercial: String?,
    @SerializedName("generic_name") val nombreGenerico: String?
)
