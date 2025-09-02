package org.dhis2.commons.service

import androidx.lifecycle.LifecycleCoroutineScope

interface SessionManagerService {
    fun onUserInteraction()

    fun checkSessionTimeout(
        navigateAction: (Int) -> Unit,
        scope: LifecycleCoroutineScope,
    ): Boolean
}
