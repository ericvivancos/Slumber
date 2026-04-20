package com.slumber.mobilehub.data

import com.slumber.mobilehub.domain.model.MobileHubSnapshot
import com.slumber.mobilehub.domain.model.QuickActionType
import kotlinx.coroutines.flow.StateFlow

interface SlumberRepository {
    val snapshot: StateFlow<MobileHubSnapshot>

    fun triggerAction(action: QuickActionType)
}
