package com.example.appmedica

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appmedica.ui.screens.LoginScreen
import com.example.appmedica.viewmodel.EstadoAutenticacion
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun tecladoAvanzaALaContrasenaYNoEnviaCamposIncompletos() {
        var envios = 0
        compose.setContent {
            MaterialTheme {
                LoginScreen(EstadoAutenticacion.SinSesion) { _, _ -> envios++ }
            }
        }

        compose.onNodeWithText("Iniciar sesión").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Correo electrónico")
            .performScrollTo().performTextInput("demo@example.com")
        compose.onNodeWithContentDescription("Correo electrónico").performImeAction()
        compose.onNodeWithContentDescription("Contraseña")
            .assertIsFocused().performImeAction()
        compose.onNodeWithText("Iniciar sesión").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle { assertEquals(0, envios) }
    }

    @Test
    fun listoEnviaUnaVezConCorreoRecortadoYContrasenaIntacta() {
        val envios = mutableListOf<Pair<String, String>>()
        compose.setContent {
            MaterialTheme {
                LoginScreen(EstadoAutenticacion.SinSesion) { correo, contrasena ->
                    envios.add(correo to contrasena)
                }
            }
        }

        compose.onNodeWithContentDescription("Correo electrónico")
            .performScrollTo().performTextInput("  demo@example.com  ")
        compose.onNodeWithContentDescription("Contraseña")
            .performScrollTo().performTextInput("  Demo123!  ")
        compose.onNodeWithContentDescription("Contraseña").performImeAction()
        compose.runOnIdle {
            assertEquals(listOf("demo@example.com" to "  Demo123!  "), envios)
        }
    }

    @Test
    fun mostrarYOcultarNoModificaLaContrasenaEnviadaPorElBoton() {
        val envios = mutableListOf<Pair<String, String>>()
        compose.setContent {
            MaterialTheme {
                LoginScreen(EstadoAutenticacion.SinSesion) { correo, contrasena ->
                    envios.add(correo to contrasena)
                }
            }
        }

        compose.onNodeWithContentDescription("Correo electrónico")
            .performScrollTo().performTextInput("demo@example.com")
        compose.onNodeWithContentDescription("Contraseña")
            .performScrollTo().performTextInput("Demo123!")
        compose.onNodeWithContentDescription("Contraseña")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
        compose.onNodeWithContentDescription("Mostrar contraseña").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Contraseña")
            .assertTextContains("Demo123!")
        compose.onNodeWithContentDescription("Ocultar contraseña").performClick()
        compose.onNodeWithContentDescription("Contraseña")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
            .assertTextContains("••••••••")
        compose.onNodeWithText("Iniciar sesión")
            .performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(listOf("demo@example.com" to "Demo123!"), envios) }
    }

    @Test
    fun cargaBloqueaLosCamposYElEnvioYPermiteReintentarTrasUnError() {
        val estado = mutableStateOf<EstadoAutenticacion>(EstadoAutenticacion.SinSesion)
        var envios = 0
        compose.setContent {
            MaterialTheme {
                LoginScreen(estado.value) { _, _ ->
                    envios++
                    estado.value = EstadoAutenticacion.Cargando
                }
            }
        }

        compose.onNodeWithContentDescription("Correo electrónico")
            .performScrollTo().performTextInput("demo@example.com")
        compose.onNodeWithContentDescription("Contraseña")
            .performScrollTo().performTextInput("Demo123!")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Iniciando sesión…").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Correo electrónico").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Contraseña").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Mostrar contraseña").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(1, envios)
            estado.value = EstadoAutenticacion.Error("Sin conexión. Inténtalo de nuevo.")
        }
        compose.onNodeWithText("Sin conexión. Inténtalo de nuevo.")
            .performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Iniciar sesión")
            .performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(2, envios) }
    }

    @Test
    fun pantallaPequenaConTextoDoblePermiteLlegarAlFormularioYEnviar() {
        var envios = 0
        compose.setContent {
            val densidad = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(densidad.density, 2f)) {
                MaterialTheme {
                    Box(Modifier.width(320.dp).height(480.dp)) {
                        LoginScreen(EstadoAutenticacion.SinSesion) { _, _ -> envios++ }
                    }
                }
            }
        }

        compose.onNodeWithContentDescription("Correo electrónico")
            .performScrollTo().assertIsDisplayed().performTextInput("demo@example.com")
        compose.onNodeWithContentDescription("Contraseña")
            .performScrollTo().assertIsDisplayed().performTextInput("Demo123!")
        compose.onNodeWithText("Iniciar sesión")
            .performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Acceso para médicos y pacientes")
            .performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, envios) }
    }
}
