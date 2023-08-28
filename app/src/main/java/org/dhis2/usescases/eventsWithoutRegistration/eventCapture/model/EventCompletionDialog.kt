package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model

import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.utils.customviews.FormBottomDialog

data class EventCompletionDialog(
    val bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    val mainButtonAction: FormBottomDialog.ActionType,
    val secondaryButtonAction: FormBottomDialog.ActionType?,
    val fieldsWithIssues: List<FieldWithIssue>,
)
