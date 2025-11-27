package org.dhis2.mobile.aggregates.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.dhis2.mobile.aggregates.model.Violation
import org.dhis2.mobile.aggregates.resources.Res
import org.dhis2.mobile.aggregates.resources.complete
import org.dhis2.mobile.aggregates.resources.complete_anyway
import org.dhis2.mobile.aggregates.resources.no
import org.dhis2.mobile.aggregates.resources.not_now
import org.dhis2.mobile.aggregates.resources.ok
import org.dhis2.mobile.aggregates.resources.review
import org.dhis2.mobile.aggregates.resources.yes
import org.dhis2.mobile.aggregates.ui.constants.COMPLETION_DIALOG_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.MANDATORY_FIELDS_DIALOG_OK_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.OPTIONAL_VALIDATION_RULE_DIALOG_ACCEPT_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_DIALOG_COMPLETE_ANYWAY_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_DIALOG_REVIEW_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.states.DataSetModalDialogUIState
import org.dhis2.mobile.aggregates.ui.states.DataSetModalType
import org.dhis2.mobile.aggregates.ui.states.DataSetModalType.COMPLETION
import org.dhis2.mobile.aggregates.ui.states.DataSetModalType.MANDATORY_FIELDS
import org.dhis2.mobile.aggregates.ui.states.DataSetModalType.VALIDATION_RULES
import org.dhis2.mobile.aggregates.ui.states.DataSetModalType.VALIDATION_RULES_ERROR
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ValidationBottomSheet(dataSetUIState: DataSetModalDialogUIState) {
    BottomSheetShell(
        modifier = Modifier.testTag(dataSetUIState.type.name),
        uiState = dataSetUIState.contentDialogUIState,
        content = {
            provideContent(
                type = dataSetUIState.type,
                violations = dataSetUIState.violations,
            )
        },
        buttonBlock = {
            provideButtonBlock(
                type = dataSetUIState.type,
                onPrimaryButtonClick = dataSetUIState.onPrimaryButtonClick,
                onSecondaryButtonClick = dataSetUIState.onSecondaryButtonClick,
                mandatory = dataSetUIState.mandatory,
                canComplete = dataSetUIState.canComplete,
            )
        },
        onDismiss = dataSetUIState.onDismiss,
        icon = {
            provideIcon(dataSetUIState.type)
        },
    )
}

@Composable
private fun provideIcon(type: DataSetModalType) {
    when (type) {
        VALIDATION_RULES_ERROR -> {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = SurfaceColor.Error,
            )
        }

        else -> {
            // No icon
        }
    }
}

@Composable
private fun provideContent(
    type: DataSetModalType,
    violations: List<Violation>?,
) {
    when (type) {
        VALIDATION_RULES_ERROR -> {
            violations?.let {
                ValidationRulesErrorDialog(
                    violations = it,
                )
            }
        }

        else -> {
            // No content
        }
    }
}

@Composable
private fun provideButtonBlock(
    type: DataSetModalType,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit,
    mandatory: Boolean,
    canComplete: Boolean,
) {
    when (type) {
        COMPLETION -> {
            ButtonBlock(
                modifier =
                    Modifier.padding(
                        BottomSheetShellDefaults.buttonBlockPaddings(),
                    ),
                primaryButton = {
                    Button(
                        style = ButtonStyle.OUTLINED,
                        text = stringResource(Res.string.not_now),
                        onClick = onPrimaryButtonClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                secondaryButton = {
                    if (canComplete) {
                        Button(
                            style = ButtonStyle.FILLED,
                            text = stringResource(Res.string.complete),
                            modifier =
                                Modifier
                                    .testTag(COMPLETION_DIALOG_BUTTON_TEST_TAG)
                                    .fillMaxWidth(),
                            onClick = onSecondaryButtonClick,
                        )
                    }
                },
            )
        }

        MANDATORY_FIELDS -> {
            ButtonBlock(
                modifier =
                    Modifier.padding(
                        BottomSheetShellDefaults.buttonBlockPaddings(),
                    ),
                primaryButton = {
                    Button(
                        style = ButtonStyle.FILLED,
                        text = stringResource(Res.string.ok),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag(MANDATORY_FIELDS_DIALOG_OK_BUTTON_TEST_TAG),
                        onClick = onPrimaryButtonClick,
                    )
                },
            )
        }

        VALIDATION_RULES -> {
            ButtonBlock(
                modifier =
                    Modifier.padding(
                        BottomSheetShellDefaults.buttonBlockPaddings(),
                    ),
                primaryButton = {
                    Button(
                        style = ButtonStyle.OUTLINED,
                        text = stringResource(Res.string.no),
                        onClick = onPrimaryButtonClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                secondaryButton = {
                    Button(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag(OPTIONAL_VALIDATION_RULE_DIALOG_ACCEPT_TEST_TAG),
                        style = ButtonStyle.FILLED,
                        text = stringResource(Res.string.yes),
                        onClick = onSecondaryButtonClick,
                    )
                },
            )
        }

        VALIDATION_RULES_ERROR -> {
            ButtonBlock(
                modifier =
                    Modifier.padding(
                        BottomSheetShellDefaults.buttonBlockPaddings(),
                    ),
                primaryButton = {
                    if (!mandatory && canComplete) {
                        Button(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .testTag(VALIDATION_DIALOG_COMPLETE_ANYWAY_BUTTON_TEST_TAG),
                            style = ButtonStyle.TEXT,
                            text = stringResource(Res.string.complete_anyway),
                            onClick = onPrimaryButtonClick,
                        )
                    }
                },
                secondaryButton = {
                    Button(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag(VALIDATION_DIALOG_REVIEW_BUTTON_TEST_TAG),
                        style = ButtonStyle.FILLED,
                        text = stringResource(Res.string.review),
                        onClick = onSecondaryButtonClick,
                    )
                },
            )
        }
    }
}
