package com.slumber.mobilehub.ui.app

import androidx.lifecycle.ViewModel
import com.slumber.mobilehub.data.FakeSlumberRepository
import com.slumber.mobilehub.data.SlumberRepository
import com.slumber.mobilehub.domain.model.QuickActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted

class SlumberViewModel(
    private val repository: SlumberRepository = FakeSlumberRepository()
) : ViewModel() {
    private val selectedDestination = MutableStateFlow(AppDestination.Dashboard)

    val uiState: StateFlow<SlumberAppUiState> = combine(
        repository.snapshot,
        selectedDestination
    ) { snapshot, destination ->
        SlumberAppUiState(
            selectedDestination = destination,
            snapshot = snapshot
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SlumberAppUiState(
            selectedDestination = AppDestination.Dashboard,
            snapshot = repository.snapshot.value
        )
    )

    fun selectDestination(destination: AppDestination) {
        selectedDestination.update { destination }
    }

    fun onQuickAction(action: QuickActionType) {
        repository.triggerAction(action)

        if (action == QuickActionType.OPEN_SETTINGS) {
            selectDestination(AppDestination.Settings)
        }

        if (action == QuickActionType.VIEW_HISTORY) {
            selectDestination(AppDestination.History)
        }
    }
}
