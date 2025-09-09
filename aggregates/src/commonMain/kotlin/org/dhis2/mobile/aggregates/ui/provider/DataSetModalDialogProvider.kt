package org.dhis2.mobile.aggregates.ui.provider

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign
import org.dhis2.mobile.aggregates.model.Violation
import org.dhis2.mobile.aggregates.ui.states.DataSetModalDialogUIState
import org.dhis2.mobile.aggregates.ui.states.DataSetModalType
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

internal class DataSetModalDialogProvider(
    val resourceManager: ResourceManager,
) {
    suspend fun provideCompletionDialog(
        onDismiss: () -> Unit,
        onNotNow: () -> Unit,
        onComplete: () -> Unit,
    ): DataSetModalDialogUIState =
        DataSetModalDialogUIState(
            contentDialogUIState =
                BottomSheetShellUIState(
                    title = resourceManager.provideCompletionDialogTitle(),
                    subtitle = resourceManager.provideCompletionDialogDescription(),
                    showBottomSectionDivider = false,
                    headerTextAlignment = TextAlign.Start,
                ),
            onDismiss = onDismiss,
            onPrimaryButtonClick = onNotNow,
            onSecondaryButtonClick = onComplete,
            type = DataSetModalType.COMPLETION,
        )

    suspend fun provideMandatoryFieldsDialog(
        mandatoryFieldsMessage: String,
        onDismiss: () -> Unit,
        onAccept: () -> Unit,
    ): DataSetModalDialogUIState =
        DataSetModalDialogUIState(
            contentDialogUIState =
                BottomSheetShellUIState(
                    title = resourceManager.provideSaved(),
                    subtitle = mandatoryFieldsMessage,
                    showBottomSectionDivider = false,
                    headerTextAlignment = TextAlign.Start,
                ),
            onDismiss = onDismiss,
            onPrimaryButtonClick = onAccept,
            type = DataSetModalType.MANDATORY_FIELDS,
        )

    suspend fun provideAskRunValidationsDialog(
        onDismiss: () -> Unit,
        onDeny: () -> Unit,
        onAccept: () -> Unit,
    ): DataSetModalDialogUIState =
        DataSetModalDialogUIState(
            contentDialogUIState =
                BottomSheetShellUIState(
                    title = resourceManager.provideSaved(),
                    subtitle = resourceManager.provideAskRunValidations(),
                    showBottomSectionDivider = false,
                    headerTextAlignment = TextAlign.Start,
                ),
            onDismiss = onDismiss,
            onPrimaryButtonClick = onDeny,
            onSecondaryButtonClick = onAccept,
            type = DataSetModalType.VALIDATION_RULES,
        )

    suspend fun provideValidationRulesErrorDialog(
        onDismiss: () -> Unit,
        onMarkAsComplete: () -> Unit,
        violations: List<Violation>,
        mandatory: Boolean,
        canComplete: Boolean,
    ): DataSetModalDialogUIState =
        DataSetModalDialogUIState(
            contentDialogUIState =
                BottomSheetShellUIState(
                    title = "${violations.size} ${
                        resourceManager.provideValidationErrorDescription(
                            violations.size,
                        )
                    }",
                    showTopSectionDivider = false,
                    showBottomSectionDivider = false,
                    contentPadding = PaddingValues(Spacing.Spacing0),
                ),
            onDismiss = onDismiss,
            onPrimaryButtonClick = onMarkAsComplete,
            onSecondaryButtonClick = onDismiss,
            type = DataSetModalType.VALIDATION_RULES_ERROR,
            violations = violations,
            mandatory = mandatory,
            canComplete = canComplete,
        )
}
