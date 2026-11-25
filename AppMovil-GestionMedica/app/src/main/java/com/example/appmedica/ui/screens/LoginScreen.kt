
package com.example.appmedica.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appmedica.R
import com.example.appmedica.ui.theme.LoginBordeCampo
import com.example.appmedica.ui.theme.LoginBotonDeshabilitado
import com.example.appmedica.ui.theme.LoginCampoFondo
import com.example.appmedica.ui.theme.LoginErrorFondo
import com.example.appmedica.ui.theme.LoginErrorTexto
import com.example.appmedica.ui.theme.LoginFondo
import com.example.appmedica.ui.theme.LoginIndicacion
import com.example.appmedica.ui.theme.LoginMenta
import com.example.appmedica.ui.theme.LoginOndaAzul
import com.example.appmedica.ui.theme.LoginOndaVerde
import com.example.appmedica.ui.theme.LoginPetroleo
import com.example.appmedica.ui.theme.LoginTexto
import com.example.appmedica.ui.theme.LoginTextoSecundario
import com.example.appmedica.viewmodel.EstadoAutenticacion

@Composable
fun LoginScreen(
    estado: EstadoAutenticacion,
    onIniciarSesion: (String, String) -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }

    val cargando = estado is EstadoAutenticacion.Cargando
    val etiquetaCorreo = stringResource(R.string.login_correo)
    val etiquetaContrasena = stringResource(R.string.login_contrasena)
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LoginTexto,
        unfocusedTextColor = LoginTexto,
        disabledTextColor = LoginTextoSecundario.copy(alpha = 0.6f),
        focusedContainerColor = LoginCampoFondo,
        unfocusedContainerColor = LoginCampoFondo,
        disabledContainerColor = LoginCampoFondo,
        focusedBorderColor = LoginPetroleo,
        unfocusedBorderColor = LoginBordeCampo,
        disabledBorderColor = LoginBordeCampo.copy(alpha = 0.65f),
        focusedLeadingIconColor = LoginPetroleo,
        unfocusedLeadingIconColor = LoginIndicacion,
        disabledLeadingIconColor = LoginIndicacion.copy(alpha = 0.6f),
        focusedTrailingIconColor = LoginPetroleo,
        unfocusedTrailingIconColor = LoginIndicacion,
        disabledTrailingIconColor = LoginIndicacion.copy(alpha = 0.6f),
        focusedPlaceholderColor = LoginIndicacion,
        unfocusedPlaceholderColor = LoginIndicacion,
        disabledPlaceholderColor = LoginIndicacion.copy(alpha = 0.6f),
        cursorColor = LoginPetroleo
    )

    Box(Modifier.fillMaxSize().background(LoginFondo)) {
        DecoracionLogin(Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EncabezadoLogin()

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = etiquetaCorreo,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = LoginTexto
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text(stringResource(R.string.login_correo_indicacion)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_login_correo),
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    enabled = !cargando,
                    shape = RoundedCornerShape(16.dp),
                    colors = coloresCampo,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .semantics { contentDescription = etiquetaCorreo }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = etiquetaContrasena,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = LoginTexto
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    placeholder = { Text(stringResource(R.string.login_contrasena_indicacion)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_login_candado),
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { contrasenaVisible = !contrasenaVisible },
                            enabled = !cargando
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (contrasenaVisible) R.drawable.ic_login_ocultar
                                    else R.drawable.ic_login_mostrar
                                ),
                                contentDescription = stringResource(
                                    if (contrasenaVisible) R.string.login_ocultar_contrasena
                                    else R.string.login_mostrar_contrasena
                                )
                            )
                        }
                    },
                    singleLine = true,
                    enabled = !cargando,
                    shape = RoundedCornerShape(16.dp),
                    colors = coloresCampo,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    visualTransformation = if (contrasenaVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .semantics { contentDescription = etiquetaContrasena }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (estado is EstadoAutenticacion.Error) {
                    Text(
                        text = estado.mensaje,
                        modifier = Modifier.fillMaxWidth()
                            .background(LoginErrorFondo, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        color = LoginErrorTexto,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        onIniciarSesion(correo.trim(), contrasena)
                    },
                    enabled = !cargando &&
                            correo.isNotBlank() &&
                            contrasena.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoginPetroleo,
                        contentColor = LoginCampoFondo,
                        disabledContainerColor = if (cargando) LoginPetroleo
                            else LoginBotonDeshabilitado,
                        disabledContentColor = if (cargando) LoginCampoFondo
                            else LoginTextoSecundario
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = LoginCampoFondo,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                    }
                    Text(
                        text = stringResource(
                            if (cargando) R.string.login_iniciando_sesion
                            else R.string.login_iniciar_sesion
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Acceso para médicos y pacientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = LoginTextoSecundario,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EncabezadoLogin() {
    Box(
        modifier = Modifier.size(96.dp).background(LoginOndaVerde, RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_login_medico),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
    }
    Spacer(Modifier.height(20.dp))
    Text(
        text = stringResource(R.string.login_titulo),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = LoginTexto,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.login_subtitulo),
        style = MaterialTheme.typography.bodyLarge,
        color = LoginTextoSecundario,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.login_lema),
        modifier = Modifier.background(LoginMenta, RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = LoginPetroleo,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun DecoracionLogin(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val ancho = size.width
        val alto = size.height
        val profundidad = 150.dp.toPx().coerceAtMost(alto * 0.22f)
        drawPath(Path().apply {
            moveTo(0f, 0f)
            lineTo(ancho * 0.85f, 0f)
            cubicTo(ancho * 0.6f, profundidad, ancho * 0.25f, profundidad * 0.3f, 0f, profundidad)
            close()
        }, LoginOndaAzul)
        drawPath(Path().apply {
            moveTo(0f, 0f)
            lineTo(ancho * 0.5f, 0f)
            quadraticTo(ancho * 0.22f, profundidad * 0.7f, 0f, profundidad * 0.6f)
            close()
        }, LoginOndaVerde.copy(alpha = 0.8f))
        drawPath(Path().apply {
            moveTo(ancho, alto)
            lineTo(ancho * 0.1f, alto)
            cubicTo(ancho * 0.55f, alto - profundidad, ancho * 0.7f, alto, ancho, alto - profundidad * 0.65f)
            close()
        }, LoginOndaAzul)
        drawPath(Path().apply {
            moveTo(ancho, alto)
            lineTo(ancho * 0.55f, alto)
            quadraticTo(ancho * 0.8f, alto - profundidad * 0.85f, ancho, alto - profundidad)
            close()
        }, LoginOndaVerde.copy(alpha = 0.65f))
    }
}

@Preview(name = "Login · identidad médica", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LoginIdentidadPreview() {
    MaterialTheme {
        LoginScreen(estado = EstadoAutenticacion.SinSesion, onIniciarSesion = { _, _ -> })
    }
}

@Preview(name = "Login · cargando", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LoginCargandoPreview() {
    MaterialTheme {
        LoginScreen(estado = EstadoAutenticacion.Cargando, onIniciarSesion = { _, _ -> })
    }
}

@Preview(name = "Login · error", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LoginErrorPreview() {
    MaterialTheme {
        LoginScreen(
            estado = EstadoAutenticacion.Error("Revisa tu correo y contraseña e inténtalo de nuevo."),
            onIniciarSesion = { _, _ -> }
        )
    }
}
