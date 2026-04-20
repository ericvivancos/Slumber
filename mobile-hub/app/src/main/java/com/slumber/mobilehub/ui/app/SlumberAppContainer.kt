package com.slumber.mobilehub.ui.app

import android.content.Context
import com.slumber.mobilehub.data.FakeSlumberRepository
import com.slumber.mobilehub.data.SlumberRepository

object SlumberAppContainer {
    @Volatile
    private var repository: SlumberRepository? = null

    fun repository(context: Context): SlumberRepository {
        return repository ?: synchronized(this) {
            repository ?: FakeSlumberRepository(context.applicationContext).also { created ->
                repository = created
            }
        }
    }
}
