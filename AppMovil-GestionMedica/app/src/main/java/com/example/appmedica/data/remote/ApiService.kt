package com.example.appmedica.data.remote

import retrofit2.http.GET

interface ApiService {
    @GET("drug/ndc.json?limit=10")
    suspend fun obtenerMedicamentos(): CatalogoMedicamentosDto
}
