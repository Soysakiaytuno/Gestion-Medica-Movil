package com.example.appmedica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmedica.viewmodel.BuscarPacienteViewModel
import com.example.appmedica.model.calcularEdad

private val Celeste = Color(0xFFD6F0FF)
private val CelesteGris = Color(0xFF9BBACC)
//private val AzulMedio = Color(0xFF568099)
//private val AzulOscuro = Color(0xFF254E66)
private val FondoOscuro = Color(0xFF082333)

@Composable
fun BuscarPacienteScreen(
    volver: () -> Unit = {},
    irAgregar: () -> Unit = {},
    irDetalles: (String) -> Unit = {},
    viewModel: BuscarPacienteViewModel = viewModel()
) {
    val pacientes = viewModel.pacientesFiltrados()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FondoOscuro)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AzulOscuro)
                    .clickable { volver() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\u2190", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Seleccionar paciente",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Button(
                onClick = irAgregar,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CelesteGris),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.6f)
                    .height(54.dp)
            ) {
                Text(
                    text = "Agregar paciente",
                    color = FondoOscuro,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val coloresCampo = TextFieldDefaults.colors(
                focusedContainerColor = Celeste,
                unfocusedContainerColor = Celeste,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = FondoOscuro,
                focusedTextColor = FondoOscuro,
                unfocusedTextColor = FondoOscuro
            )

            TextField(
                value = viewModel.query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text(text = "Buscar por nombre", color = AzulMedio) },
                trailingIcon = { Text(text = "\uD83D\uDD0D") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = coloresCampo,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Pacientes",
                color = FondoOscuro,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (pacientes.isEmpty()) {
                Text(text = "No se encontraron pacientes", color = AzulMedio)
            }

            pacientes.forEach { paciente ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CelesteGris)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nombre: ${paciente.nombre} ${paciente.apellidoPaterno}",
                            color = FondoOscuro
                        )
                        Text(
                            text = "Edad: ${calcularEdad(paciente.fechaNacimiento)} años",
                            color = FondoOscuro
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Celeste)
                            .clickable { irDetalles(paciente.id) }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Mas detalles", color = FondoOscuro)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}