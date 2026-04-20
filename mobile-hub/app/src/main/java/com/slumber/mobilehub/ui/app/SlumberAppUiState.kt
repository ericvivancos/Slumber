package com.slumber.mobilehub.ui.app

import com.slumber.mobilehub.domain.model.MobileHubSnapshot

enum class AppDestination(val label: String) {
    Dashboard("Dashboard"),
    History("Historial"),
    Settings("Reglas")
}

data class SlumberAppUiState(
    val selectedDestination: AppDestination,
    val snapshot: MobileHubSnapshot
)
