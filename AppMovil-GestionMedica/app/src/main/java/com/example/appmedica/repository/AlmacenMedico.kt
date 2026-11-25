package com.example.appmedica.repository

import androidx.room.withTransaction
import com.example.appmedica.data.local.AppDatabase
import com.example.appmedica.data.remote.DatosMedicosDto
import com.example.appmedica.data.remote.SupabaseApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

/** Room contiene exclusivamente la última respuesta del servidor de esta cuenta. */
class AlmacenMedico(
    val database: AppDatabase,
    private val api: () -> SupabaseApi,
    private val token: suspend () -> String
) {
    private val mutex = Mutex()

    suspend fun actualizar() = ejecutar { datos(it) }

    suspend fun ejecutar(operacion: suspend SupabaseApi.(String) -> DatosMedicosDto) = mutex.withLock {
        val datos = try {
            api().operacion(token())
        } catch (e: CancellationException) { throw e
        } catch (e: HttpException) {
            val mensaje = when (e.code()) {
                401 -> "No se pudo verificar tu sesión. Vuelve a iniciar sesión."
                403 -> "Tu cuenta no permite realizar esta acción. Si necesitas ayuda, contacta al responsable."
                409 -> "La información cambió o ya está registrada. Actualiza la lista antes de intentarlo de nuevo."
                400 -> "Revisa los datos ingresados e inténtalo de nuevo."
                else -> "El servicio no está disponible en este momento. Inténtalo más tarde."
            }
            throw ErrorOperacion(mensaje, e)
        } catch (e: IOException) {
            throw ErrorOperacion("No pudimos confirmar la respuesta. Revisa tu conexión y actualiza la lista antes de repetir la acción.", e)
        } catch (e: ErrorOperacion) {
            throw e
        } catch (e: Exception) {
            throw ErrorOperacion("No se pudo completar la acción. Inténtalo más tarde; si el problema continúa, contacta al responsable.", e)
        }
        try {
            database.withTransaction {
                database.citaDao().vaciar()
                database.pacienteDao().vaciar()
                database.pacienteDao().insertarTodos(datos.pacientes.map { it.toEntity() })
                database.citaDao().insertarTodas(datos.citas.map { it.toEntity() })
            }
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            throw ErrorOperacion("No se pudieron mostrar los últimos cambios. Desliza hacia abajo para actualizar antes de repetir la acción.", e)
        }
    }
}
