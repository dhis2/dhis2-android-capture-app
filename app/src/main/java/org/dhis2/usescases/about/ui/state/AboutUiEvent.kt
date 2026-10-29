package org.dhis2.usescases.about.ui.state

import org.dhis2.usescases.about.model.AboutLink

sealed interface AboutUiEvent {
    data class OpenLink(
        val link: AboutLink,
    ) : AboutUiEvent

    data object ShowNoConnection : AboutUiEvent
}
