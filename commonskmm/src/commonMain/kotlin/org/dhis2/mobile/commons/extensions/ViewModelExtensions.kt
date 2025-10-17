package org.dhis2.mobile.commons.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.coroutine.CoroutineTracker

/**
 * Launches a coroutine in the ViewModel's scope for executing a use case.
 * It simplifies running asynchronous operations within a [ViewModel] and integrates with [org.dhis2.mobile.commons.coroutine.CoroutineTracker]
 * to monitor active coroutines, which can be useful for testing or debugging purposes.
 *
 * @param block The suspend block of code to be executed within the coroutine. This is typically the use case logic.
 */
fun ViewModel.launchUseCase(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch {
        CoroutineTracker.increment()
        try {
            block()
        } finally {
            CoroutineTracker.decrement()
        }
    }
