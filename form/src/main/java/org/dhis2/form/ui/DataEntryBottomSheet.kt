package org.dhis2.form.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.commons.dialogs.bottomsheet.ErrorFieldList
import org.dhis2.commons.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.commons.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.commons.dialogs.bottomsheet.SECONDARY_BUTTON_TAG
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing24
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
internal fun DataEntryBottomSheet(
    model: BottomSheetDialogUiModel,
    allowDiscard: Boolean,
    fieldsWithIssues: List<FieldWithIssue>,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit,
    onDiscardChanges: () -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetShell(
        uiState =
            BottomSheetShellUIState(
                title = model.title,
                subtitle = model.subtitle,
                description = model.message,
                showTopSectionDivider = true,
                showBottomSectionDivider = fieldsWithIssues.isNotEmpty(),
                headerTextAlignment = model.headerTextAlignment,
            ),
        content =
            if (fieldsWithIssues.isEmpty()) {
                null
            } else {
                {
                    ErrorFieldList(
                        fieldsWithIssues = fieldsWithIssues,
                        onItemClick = {
                            onDismiss()
                        },
                    )
                }
            },
        icon =
            model.iconResource.takeIf { it != -1 }?.let { iconResource ->
                {
                    Icon(
                        modifier = Modifier.size(Spacing24),
                        painter = painterResource(iconResource),
                        contentDescription = "Icon",
                        tint = SurfaceColor.Primary,
                    )
                }
            },
        buttonBlock = {
            DataEntryButtonBlock(
                model = model,
                allowDiscard = allowDiscard,
                onPrimaryButtonClick = onPrimaryButtonClick,
                onSecondaryButtonClick = onSecondaryButtonClick,
                onDiscardChanges = onDiscardChanges,
                onDismiss = onDismiss,
            )
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun DataEntryButtonBlock(
    model: BottomSheetDialogUiModel,
    allowDiscard: Boolean,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit,
    onDiscardChanges: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (model.hasButtons()) {
        ButtonBlock(
            modifier = Modifier.padding(BottomSheetShellDefaults.buttonBlockPaddings()),
            primaryButton = {
                model.secondaryButton?.let { secondaryButton ->
                    Button(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag(SECONDARY_BUTTON_TAG),
                        style = secondaryButton.buttonStyle,
                        text =
                            secondaryButton.textLabel
                                ?: stringResource(secondaryButton.textResource),
                        colorStyle = getColorStyle(secondaryButton),
                        onClick = {
                            onDiscardChanges.takeIf { allowDiscard }?.invoke()
                            onSecondaryButtonClick()
                            onDismiss()
                        },
                    )
                }
            },
            secondaryButton = {
                model.mainButton?.let { mainButton ->
                    Button(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag(MAIN_BUTTON_TAG),
                        style = mainButton.buttonStyle,
                        text =
                            mainButton.textLabel ?: stringResource(
                                mainButton.textResource,
                            ),
                        colorStyle = getColorStyle(mainButton),
                        onClick = {
                            onPrimaryButtonClick()
                            onDismiss()
                        },
                    )
                }
            },
        )
    }
}

private fun getColorStyle(style: DialogButtonStyle) =
    when (style) {
        is DialogButtonStyle.DiscardButton -> ColorStyle.WARNING
        else -> ColorStyle.DEFAULT
    }
