package org.dhis2.mobile.commons.coroutine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Test implementation of [launchUseCase] that defaults to [Dispatchers.Main].
 * This allows tests to control its execution via [Dispatchers.setMain].
 * The original implementation in `commonMain` defaults to `Dispatchers.IO`.
 */
fun ViewModel.launchUseCase(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit,
) {
    viewModelScope.launch(dispatcher) {
        // CoroutineTracker is kept for consistency,
        // but it's not the main focus for this test implementation.
        CoroutineTracker.increment()
        try {
            block()
        } finally {
            CoroutineTracker.decrement()
        }
    }
}
