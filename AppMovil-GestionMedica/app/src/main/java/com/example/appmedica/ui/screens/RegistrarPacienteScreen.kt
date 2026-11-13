package com.example.appmedica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmedica.ui.theme.AzulClaro
import com.example.appmedica.ui.theme.AzulOscuro
import com.example.appmedica.ui.theme.AzulPildora
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco
import com.example.appmedica.ui.theme.FondoVolverRegistro
import com.example.appmedica.ui.theme.TextoError
import com.example.appmedica.ui.theme.TextoOscuro
import com.example.appmedica.viewmodel.RegistroPacienteFormulario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarPacienteScreen(
    formulario: RegistroPacienteFormulario,
    mensajeError: String,
    guardando: Boolean,
    onFormularioChange: ((RegistroPacienteFormulario) -> RegistroPacienteFormulario) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Registrar Paciente",
                        color = FondoBlanco,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(FondoVolverRegistro, shape = RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "<",
                            color = FondoBlanco,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPrincipal)
            )
        }
    ) { paddingValues ->

        FormularioPaciente(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding(),
            formulario = formulario,
            mensajeError = mensajeError,
            guardando = guardando,
            onFormularioChange = onFormularioChange,
            onGuardar = onGuardar
        )
    }
}

@Composable
fun FormularioPaciente(
    modifier: Modifier = Modifier,
    formulario: RegistroPacienteFormulario,
    mensajeError: String,
    guardando: Boolean,
    onFormularioChange: ((RegistroPacienteFormulario) -> RegistroPacienteFormulario) -> Unit,
    onGuardar: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoBlanco)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        CampoPersonalizado("Nombre:", formulario.nombre) { valor ->
            onFormularioChange { it.copy(nombre = valor) }
        }
        CampoPersonalizado("Apellido Paterno:", formulario.apellidoPaterno) { valor ->
            onFormularioChange { it.copy(apellidoPaterno = valor) }
        }
        CampoPersonalizado("Apellido Materno:", formulario.apellidoMaterno) { valor ->
            onFormularioChange { it.copy(apellidoMaterno = valor) }
        }
        CampoPersonalizado("CI (7 u 8 dígitos):", formulario.ci, KeyboardType.Number) { valor ->
            onFormularioChange { it.copy(ci = valor) }
        }
        CampoPersonalizado("Fecha Nacimiento (AAAA-MM-DD):", formulario.fechaNacimiento) { valor ->
            onFormularioChange { it.copy(fechaNacimiento = valor) }
        }
        CampoPersonalizado("Email:", formulario.email, KeyboardType.Email) { valor ->
            onFormularioChange { it.copy(email = valor) }
        }
        CampoPersonalizado("Celular (mínimo 8 dígitos):", formulario.celular, KeyboardType.Phone) { valor ->
            onFormularioChange { it.copy(celular = valor) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Agregar imagen (pendiente)", color = AzulPrincipal)

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotBlank()) {
            Text(
                text = mensajeError,
                color = TextoError,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = onGuardar,
            enabled = !guardando,
            colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Guardar Paciente", color = FondoBlanco)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CampoPersonalizado(
    etiqueta: String,
    valor: String,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    onValorCambiado: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulPildora, CircleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text = etiqueta, color = TextoOscuro, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = valor,
            onValueChange = onValorCambiado,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AzulClaro,
                unfocusedContainerColor = AzulClaro,
                focusedTextColor = TextoOscuro,
                unfocusedTextColor = TextoOscuro,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AzulPrincipal
            ),
            shape = CircleShape,
            singleLine = true
        )
    }
}
