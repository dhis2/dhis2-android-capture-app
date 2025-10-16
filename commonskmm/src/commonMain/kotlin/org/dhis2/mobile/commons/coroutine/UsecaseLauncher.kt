package org.dhis2.mobile.commons.coroutine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Launches a coroutine in the ViewModel's scope for executing a use case.
 * It simplifies running asynchronous operations within a [ViewModel] and integrates with [CoroutineTracker]
 * to monitor active coroutines, which can be useful for testing or debugging purposes.
 *
 * @param dispatcher The [CoroutineDispatcher] on which the coroutine will be executed. Defaults to [Dispatchers.IO].
 * @param block The suspend block of code to be executed within the coroutine. This is typically the use case logic.
 */
fun ViewModel.launchUseCase(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: suspend CoroutineScope.() -> Unit,
) = viewModelScope.launch(dispatcher) {
    CoroutineTracker.increment()
    try {
        block()
    } finally {
        CoroutineTracker.decrement()
    }
}
