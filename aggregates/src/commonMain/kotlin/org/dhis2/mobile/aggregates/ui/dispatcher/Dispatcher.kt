package org.dhis2.mobile.aggregates.ui.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

class Dispatcher(
    val main: () -> CoroutineDispatcher,
    val io: () -> CoroutineDispatcher,
    val default: () -> CoroutineDispatcher,
)
