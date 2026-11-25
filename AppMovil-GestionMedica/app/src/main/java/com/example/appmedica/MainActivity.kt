
package com.example.appmedica

import android.os.Bundle
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.ui.platform.LocalContext
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.viewmodel.SesionMedicaViewModel
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmedica.navigation.AppNavigation
import com.example.appmedica.ui.screens.LoginScreen
import com.example.appmedica.viewmodel.AuthViewModel
import com.example.appmedica.viewmodel.EstadoAutenticacion

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {

                val authViewModel: AuthViewModel = viewModel {
                    AuthViewModel()
                }

                val estadoAuth by authViewModel.estado
                    .collectAsStateWithLifecycle()

                when (val estado = estadoAuth) {

                    is EstadoAutenticacion.Autenticado -> {

                        key(estado.sesion.perfil) {
                            SesionMedica(estado.sesion.perfil, authViewModel::cerrarSesion)
                        }
                    }

                    else -> {
                        LoginScreen(
                            estado = estado,
                            onIniciarSesion = authViewModel::iniciarSesion
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SesionMedica(perfil: PerfilUsuario, cerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner
    ) {
        val sesion: SesionMedicaViewModel = viewModel {
            SesionMedicaViewModel(context.applicationContext, perfil)
        }
        val sync by sesion.estado.collectAsStateWithLifecycle()
        // Carga inicial por sesión. Después se consulta al deslizar o guardar cambios.
        LaunchedEffect(sesion) { sesion.actualizar() }
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            if (sync.error.isNotBlank()) {
                Text(sync.error, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Box(Modifier.weight(1f)) {
                AppNavigation(sesion.pacientes, sesion.citas, perfil, sync.cargando, sesion::actualizar, cerrarSesion)
            }
        }
    }
}
