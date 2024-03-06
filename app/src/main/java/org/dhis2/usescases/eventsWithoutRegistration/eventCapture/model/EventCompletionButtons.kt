package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model

import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.utils.customviews.FormBottomDialog

data class EventCompletionButtons(
    val buttonStyle: DialogButtonStyle,
    val action: FormBottomDialog.ActionType
)
