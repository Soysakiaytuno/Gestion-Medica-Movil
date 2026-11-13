package com.example.appmedica

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appmedica.ui.screens.CatalogoMedicamentosScreen
import com.example.appmedica.viewmodel.CatalogoMedicamentosEstado
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogoMedicamentosScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun muestraLaCargaSinOfrecerReintentoMientrasConsulta() {
        compose.setContent {
            CatalogoMedicamentosScreen(
                estado = CatalogoMedicamentosEstado.Loading,
                reintentar = {},
                volver = {}
            )
        }

        compose.onNodeWithText("Consultando openFDA…").assertIsDisplayed()
        compose.onNodeWithText("Reintentar").assertDoesNotExist()
    }
}
