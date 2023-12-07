package org.dhis2.usescases.eventswithoutregistration.eventcapture.model

import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.utils.customviews.FormBottomDialog

data class EventCompletionButtons(
    val buttonStyle: DialogButtonStyle,
    val action: FormBottomDialog.ActionType,
)
