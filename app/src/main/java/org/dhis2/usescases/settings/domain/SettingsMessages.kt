package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class SettingsMessages {
    private val _messageChannel = Channel<String>(Channel.BUFFERED)
    val messageChannel = _messageChannel.receiveAsFlow()

    suspend fun sendMessage(message: String) {
        _messageChannel.send(message)
    }

    fun close() {
        _messageChannel.close()
    }
}
