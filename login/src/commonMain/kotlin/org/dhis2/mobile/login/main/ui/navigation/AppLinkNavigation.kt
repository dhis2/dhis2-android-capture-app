package org.dhis2.mobile.login.main.ui.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class AppLinkNavigation {
    private val _appLink = Channel<String>(capacity = Channel.BUFFERED)
    val appLink: Flow<String> = _appLink.receiveAsFlow()

    fun emit(url: String) {
        _appLink.trySend(url)
    }
}
