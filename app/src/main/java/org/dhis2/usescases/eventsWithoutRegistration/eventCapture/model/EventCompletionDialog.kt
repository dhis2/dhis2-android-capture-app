package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model

import org.dhis2.ui.DataEntryDialogUiModel

data class EventCompletionDialog(
    val dataEntryDialogUiModel: DataEntryDialogUiModel,
    val mainButton: EventCompletionButtons,
    val secondaryButton: EventCompletionButtons
)
