package com.example.appmedica.data.remote

import com.google.gson.GsonBuilder

object SupabaseJson {
    // PostgREST necesita p_id:null para resolver la firma de guardar_cita al crear.
    val gson = GsonBuilder().serializeNulls().create()
}
