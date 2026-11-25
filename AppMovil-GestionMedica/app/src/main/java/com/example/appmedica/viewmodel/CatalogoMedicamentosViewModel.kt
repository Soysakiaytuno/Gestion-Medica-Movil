package com.example.appmedica.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.repository.CatalogoMedicamentosRepository
import com.example.appmedica.repository.MedicamentoRemoto
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface CatalogoMedicamentosEstado {
    data object Loading : CatalogoMedicamentosEstado
    data class Success(val medicamentos: List<MedicamentoRemoto>) : CatalogoMedicamentosEstado
    data class Error(val mensaje: String) : CatalogoMedicamentosEstado
}

class CatalogoMedicamentosViewModel(
    private val repository: CatalogoMedicamentosRepository
) : ViewModel() {
    private val estadoMutable = MutableStateFlow<CatalogoMedicamentosEstado>(CatalogoMedicamentosEstado.Loading)
    val estado = estadoMutable.asStateFlow()
    private var solicitud: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        if (solicitud?.isActive == true) return
        estadoMutable.value = CatalogoMedicamentosEstado.Loading
        solicitud = viewModelScope.launch {
            estadoMutable.value = try {
                CatalogoMedicamentosEstado.Success(repository.obtenerMedicamentos())
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                val mensaje = when (error) {
                    is HttpException -> if (error.code() == 429) {
                        "El catálogo está ocupado. Espera un momento y vuelve a intentarlo."
                    } else {
                        "El catálogo no está disponible en este momento. Inténtalo más tarde."
                    }
                    is JsonParseException, is MalformedJsonException -> "No se pudieron mostrar los medicamentos. Vuelve a intentarlo."
                    is IOException -> "No se pudo cargar el catálogo. Revisa tu conexión y vuelve a intentarlo."
                    else -> "No se pudo cargar el catálogo de medicamentos."
                }
                CatalogoMedicamentosEstado.Error(mensaje)
            }
        }
    }
}
