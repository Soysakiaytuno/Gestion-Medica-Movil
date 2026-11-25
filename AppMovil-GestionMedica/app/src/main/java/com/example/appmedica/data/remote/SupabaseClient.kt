package com.example.appmedica.data.remote

import com.example.appmedica.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    fun crear(): SupabaseApi {
        val url = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
        val clave = BuildConfig.SUPABASE_PUBLIC_KEY.trim()
        check(url.startsWith("https://") && clave.isNotBlank()) {
            "Falta configurar Supabase. Consulta docs/SUPABASE.md."
        }
        check(!clave.startsWith("sb_secret_") && !esServiceRole(clave)) {
            "Usa una clave pública de Supabase, nunca service_role."
        }
        return Retrofit.Builder().baseUrl("$url/")
            .client(OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().header("apikey", clave).build())
                }.build())
            .addConverterFactory(GsonConverterFactory.create(SupabaseJson.gson)).build().create(SupabaseApi::class.java)
    }

    private fun esServiceRole(clave: String): Boolean = runCatching {
        val payload = android.util.Base64.decode(clave.split('.')[1], android.util.Base64.URL_SAFE)
        org.json.JSONObject(String(payload, Charsets.UTF_8)).optString("role") == "service_role"
    }.getOrDefault(false)

    suspend fun token(uid: String): String {
        val usuario = FirebaseAuth.getInstance().currentUser
        check(usuario?.uid == uid) { "La sesión cambió. Vuelve a iniciar sesión." }
        val resultado = usuario!!.getIdToken(false).await()
        // Se conserva el token normal de Firebase. La función consulta el perfil
        // de Firestore con esa sesión; no requiere claims ni usuarios de Supabase.
        return "Bearer ${checkNotNull(resultado.token)}"
    }
}
