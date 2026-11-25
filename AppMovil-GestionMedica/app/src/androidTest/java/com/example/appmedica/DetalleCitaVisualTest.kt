package com.example.appmedica

import android.graphics.Bitmap
import android.content.ContentValues
import android.provider.MediaStore
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appmedica.model.*
import com.example.appmedica.ui.screens.DetalleCitaScreen
import com.example.appmedica.viewmodel.CitasEstado
import com.example.appmedica.viewmodel.DetalleCitaDatos
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 29)
class DetalleCitaVisualTest {
    @get:Rule val compose = createComposeRule()
    private val paciente = Paciente("pac-visual", "Ana", "López", "", "7654321",
        "ana@example.com", "70012345", emptyList(), LocalDate.of(2000, 1, 1))
    private val cita = Cita("cita-visual", paciente.id, "2026-12-24", "09:30",
        "Control médico general y revisión de resultados.", EstadoCita.PENDIENTE)

    private fun mostrar(rol: RolUsuario) {
        compose.setContent {
            DetalleCitaScreen(cita.id, DetalleCitaDatos(cita, cargando = false), listOf(paciente),
                CitasEstado(rol, paciente.id), {}, {}, {}, {})
        }
        compose.onNodeWithText("Ana López").assertIsDisplayed()
        val etapa = InstrumentationRegistry.getArguments().getString("captura", "despues")
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "detalle-cita-${rol.name.lowercase()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AppMedicaPruebas/$etapa")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }))
        resolver.openOutputStream(uri)!!.use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
    }

    @Test fun detalleMedicoConUnApellido() {
        mostrar(RolUsuario.MEDICO)
        compose.onNodeWithContentDescription("Editar cita").assertIsDisplayed()
        compose.onNodeWithText("Aceptar cita").assertDoesNotExist()
    }

    @Test fun detallePacienteConUnApellido() {
        mostrar(RolUsuario.PACIENTE)
        compose.onNodeWithText("Aceptar cita").assertIsDisplayed()
        compose.onNodeWithContentDescription("Editar cita").assertDoesNotExist()
    }
}
