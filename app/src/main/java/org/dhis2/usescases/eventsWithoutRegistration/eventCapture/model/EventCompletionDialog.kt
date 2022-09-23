package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model

import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.utils.customviews.FormBottomDialog

data class EventCompletionDialog(
    val bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    val mainButtonAction: FormBottomDialog.ActionType,
    val secondaryButtonAction: FormBottomDialog.ActionType
)
