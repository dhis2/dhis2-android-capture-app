package org.dhis2.commons.viewmodel

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    fun io(): CoroutineDispatcher
    fun computation(): CoroutineDispatcher
    fun ui(): CoroutineDispatcher
}
