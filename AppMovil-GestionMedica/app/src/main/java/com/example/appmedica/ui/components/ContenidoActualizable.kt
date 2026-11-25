package com.example.appmedica.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.example.appmedica.ui.theme.AzulOscuro
import com.example.appmedica.ui.theme.FondoBlanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoActualizable(
    actualizando: Boolean,
    onActualizar: () -> Unit,
    contenido: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = actualizando,
        onRefresh = { if (!actualizando) onActualizar() },
        state = state,
        modifier = Modifier.fillMaxSize().testTag("contenido_actualizable").semantics {
            customActions = if (actualizando) emptyList() else listOf(
                CustomAccessibilityAction("Actualizar contenido") { onActualizar(); true }
            )
        },
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = actualizando, state = state,
                containerColor = FondoBlanco, color = AzulOscuro
            )
        },
        content = contenido
    )
}
