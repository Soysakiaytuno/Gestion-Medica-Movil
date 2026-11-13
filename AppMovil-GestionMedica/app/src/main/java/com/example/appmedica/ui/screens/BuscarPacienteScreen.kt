package com.example.appmedica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmedica.model.Paciente
import com.example.appmedica.model.calcularEdad
import com.example.appmedica.ui.theme.AzulClaro
import com.example.appmedica.ui.theme.AzulMedio
import com.example.appmedica.ui.theme.AzulOscuro
import com.example.appmedica.ui.theme.AzulPildora
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco

@Composable
fun BuscarPacienteScreen(
    query: String,
    pacientes: List<Paciente>,
    onQueryChange: (String) -> Unit,
    volver: () -> Unit = {},
    irAgregar: () -> Unit = {},
    irDetalles: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoBlanco)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulPrincipal)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = volver,
                modifier = Modifier.background(AzulOscuro, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = FondoBlanco)
            }

            Text(
                text = "Seleccionar paciente",
                color = FondoBlanco,
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
                colors = ButtonDefaults.buttonColors(containerColor = AzulPildora),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.6f)
                    .height(54.dp)
            ) {
                Text(
                    text = "Agregar paciente",
                    color = AzulPrincipal,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val coloresCampo = TextFieldDefaults.colors(
                focusedContainerColor = AzulClaro,
                unfocusedContainerColor = AzulClaro,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AzulPrincipal,
                focusedTextColor = AzulPrincipal,
                unfocusedTextColor = AzulPrincipal
            )

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(text = "Buscar por nombre", color = AzulMedio) },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AzulOscuro) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = coloresCampo,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Pacientes",
                color = AzulPrincipal,
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
                        .border(1.dp, AzulPildora)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nombre: ${paciente.nombre} ${paciente.apellidoPaterno}",
                            color = AzulPrincipal
                        )
                        Text(
                            text = "Edad: ${calcularEdad(paciente.fechaNacimiento)} años",
                            color = AzulPrincipal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AzulClaro)
                            .heightIn(min = 48.dp)
                            .clickable { irDetalles(paciente.id) }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Más detalles", color = AzulPrincipal)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
