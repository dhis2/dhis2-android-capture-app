package org.dhis2.commons.filters

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

fun FilterManager.initFlow() = MutableStateFlow(0)

fun FilterManager.emit(
    scope: CoroutineScope,
    flow: MutableSharedFlow<Int>,
) {
    scope.launch {
        flow.emit(Random.nextInt())
    }
}
