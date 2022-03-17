package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model

import org.dhis2.ui.DataEntryDialogUiModel
import org.dhis2.utils.customviews.FormBottomDialog

data class EventCompletionDialog(
    val dataEntryDialogUiModel: DataEntryDialogUiModel,
    val mainButtonAction: FormBottomDialog.ActionType,
    val secondaryButtonAction: FormBottomDialog.ActionType
)
