package com.example.appmedica.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmedica.ui.theme.AzulClaro
import com.example.appmedica.ui.theme.AzulOscuro
import com.example.appmedica.ui.theme.AzulPrincipal
import com.example.appmedica.ui.theme.FondoBlanco
import com.example.appmedica.ui.theme.TextoError
import com.example.appmedica.viewmodel.CatalogoMedicamentosEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoMedicamentosScreen(
    estado: CatalogoMedicamentosEstado,
    reintentar: () -> Unit,
    volver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo openFDA") },
                navigationIcon = { TextButton(onClick = volver) { Text("Volver", color = FondoBlanco) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulPrincipal,
                    titleContentColor = FondoBlanco
                )
            )
        },
        containerColor = FondoBlanco
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Directorio público de medicamentos de EE. UU. · Datos remotos de openFDA.",
                    color = AzulPrincipal
                )
                Text("Las citas y los pacientes se guardan localmente en Room.", color = AzulPrincipal)
            }
            when (estado) {
                CatalogoMedicamentosEstado.Loading -> item {
                    CircularProgressIndicator(color = AzulOscuro)
                    Text("Consultando openFDA…", color = AzulPrincipal)
                }
                is CatalogoMedicamentosEstado.Error -> item {
                    Text(estado.mensaje, color = TextoError)
                    Button(
                        onClick = reintentar,
                        colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro, contentColor = FondoBlanco)
                    ) {
                        Text("Reintentar")
                    }
                }
                is CatalogoMedicamentosEstado.Success -> {
                    if (estado.medicamentos.isEmpty()) {
                        item { Text("openFDA no devolvió medicamentos para mostrar.", color = AzulPrincipal) }
                    }
                    items(estado.medicamentos) { medicamento ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AzulClaro, contentColor = AzulPrincipal)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(medicamento.nombreComercial, style = MaterialTheme.typography.titleMedium)
                                medicamento.nombreGenerico?.let { Text(it) }
                                Text("NDC: ${medicamento.codigoNdc}")
                            }
                        }
                    }
                }
            }
        }
    }
}
