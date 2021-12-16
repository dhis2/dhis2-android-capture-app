package org.dhis2.form.model

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    fun io(): CoroutineDispatcher
    fun computation(): CoroutineDispatcher
    fun ui(): CoroutineDispatcher
}
