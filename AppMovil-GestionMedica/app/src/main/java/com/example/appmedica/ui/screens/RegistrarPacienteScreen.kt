package com.example.appmedica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmedica.model.Paciente
import com.example.appmedica.viewmodel.RegistroViewModel

val AzulClaro = Color(0xFFD6F0FF)
val AzulPildora = Color(0xFF9BBACC)
val AzulMedio = Color(0xFF568099)
val AzulOscuro = Color(0xFF254E66)
val AzulPrincipal = Color(0xFF082333)
val FondoBlanco = Color(0xFFFFFFFF)
val TextoOscuro = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarPacienteScreen(
    onGuardarPaciente: (Paciente) -> Unit,
    onVolver: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
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
                            .background(Color(0xFF163445), shape = RoundedCornerShape(8.dp))
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
            modifier = Modifier.padding(paddingValues),
            nombre = viewModel.nombre,
            apellidoPaterno = viewModel.apellidoPaterno,
            apellidoMaterno = viewModel.apellidoMaterno,
            ci = viewModel.ci,
            fechaNacimiento = viewModel.fechaNacimiento,
            email = viewModel.email,
            celular = viewModel.celular,
            mensajeError = viewModel.mensajeError,

            onNombreChange = { viewModel.nombre = it },
            onApellidoPaternoChange = { viewModel.apellidoPaterno = it },
            onApellidoMaternoChange = { viewModel.apellidoMaterno = it },
            onCiChange = { viewModel.ci = it },
            onFechaNacimientoChange = { viewModel.fechaNacimiento = it },
            onEmailChange = { viewModel.email = it },
            onCelularChange = { viewModel.celular = it },

            onGuardar = { viewModel.onGuardar(onGuardarPaciente) }
        )
    }
}

@Composable
fun FormularioPaciente(
    modifier: Modifier = Modifier,
    nombre: String,
    apellidoPaterno: String,
    apellidoMaterno: String,
    ci: String,
    fechaNacimiento: String,
    email: String,
    celular: String,
    mensajeError: String,
    onNombreChange: (String) -> Unit,
    onApellidoPaternoChange: (String) -> Unit,
    onApellidoMaternoChange: (String) -> Unit,
    onCiChange: (String) -> Unit,
    onFechaNacimientoChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCelularChange: (String) -> Unit,
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

        CampoPersonalizado("Nombre:", nombre, onValorCambiado = onNombreChange)
        CampoPersonalizado("Apellido Paterno:", apellidoPaterno, onValorCambiado = onApellidoPaternoChange)
        CampoPersonalizado("Apellido Materno:", apellidoMaterno, onValorCambiado = onApellidoMaternoChange)
        CampoPersonalizado("CI:", ci, KeyboardType.Number, onCiChange)
        CampoPersonalizado("Fecha Nacimiento (AAAA-MM-DD):", fechaNacimiento, onValorCambiado = onFechaNacimientoChange)
        CampoPersonalizado("Email:", email, KeyboardType.Email, onEmailChange)
        CampoPersonalizado("Numero de Celular:", celular, KeyboardType.Phone, onCelularChange)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Lógica futura para imágenes */ },
            colors = ButtonDefaults.buttonColors(containerColor = AzulPildora),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
        ) {
            Text("Agregar imagen", color = TextoOscuro)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotBlank()) {
            Text(
                text = mensajeError,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = onGuardar,
            colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(48.dp)
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