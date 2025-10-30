package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.usescases.settings.SettingItem
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveIntegerOrZero
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState

@Composable
internal fun SyncParametersSettingItem(
    syncParametersViewModel: SyncParametersViewModel,
    isOpened: Boolean,
    onClick: () -> Unit,
    onScopeLimitSelected: (LimitScope) -> Unit,
    onEventToDownloadLimitUpdate: (Int) -> Unit,
    onTeiToDownloadLimitUpdate: (Int) -> Unit,
    onSpecificProgramSettingsClick: () -> Unit,
) {
    val additionalInfoList = provideInfoItems(syncParametersViewModel)
    SettingItem(
        modifier = Modifier.testTag(SettingItem.SYNC_PARAMETERS.name),
        title = stringResource(id = R.string.settingsSyncParameters),
        additionalInfoList = additionalInfoList,
        icon = Icons.Outlined.DataUsage,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (syncParametersViewModel.limitScopeIsEditable) {
                    val downloadLimitScopes =
                        listOf(
                            stringResource(R.string.settings_limit_globally),
                            stringResource(R.string.settings_limit_ou),
                            stringResource(R.string.settings_limit_program),
                            stringResource(R.string.settings_limit_ou_program),
                        )
                    var inputSettingsLimitedState =
                        remember {
                            InputShellState.UNFOCUSED
                        }
                    InputDropDown(
                        modifier = Modifier.testTag(TEST_TAG_SYNC_PARAMETERS_LIMIT_SCOPE),
                        title = stringResource(R.string.settings_limit_scope),
                        state = inputSettingsLimitedState,
                        itemCount = downloadLimitScopes.size,
                        onSearchOption = {},
                        fetchItem = { index ->
                            DropdownItem(downloadLimitScopes[index])
                        },
                        selectedItem =
                            DropdownItem(
                                label =
                                    when (syncParametersViewModel.limitScope) {
                                        LimitScope.ALL_ORG_UNITS,
                                        LimitScope.GLOBAL,
                                        -> downloadLimitScopes[0]

                                        LimitScope.PER_ORG_UNIT -> downloadLimitScopes[1]
                                        LimitScope.PER_PROGRAM -> downloadLimitScopes[2]
                                        LimitScope.PER_OU_AND_PROGRAM -> downloadLimitScopes[3]
                                    },
                            ),
                        onResetButtonClicked = {
                            onScopeLimitSelected(LimitScope.GLOBAL)
                        },
                        onItemSelected = { index, _ ->
                            val selectedScope =
                                when (index) {
                                    0 -> LimitScope.GLOBAL
                                    1 -> LimitScope.PER_ORG_UNIT
                                    2 -> LimitScope.PER_PROGRAM
                                    3 -> LimitScope.PER_OU_AND_PROGRAM
                                    else -> LimitScope.GLOBAL
                                }
                            inputSettingsLimitedState = InputShellState.UNFOCUSED
                            onScopeLimitSelected(selectedScope)
                        },
                        loadOptions = {},
                        showDeleteButton = false,
                    )

                    var eventsToDownload =
                        remember {
                            TextFieldValue(
                                text = syncParametersViewModel.numberOfEventsToDownload.toString(),
                                selection = TextRange(syncParametersViewModel.numberOfEventsToDownload.toString().length),
                            )
                        }
                    var inputEventsToDownloadState =
                        remember {
                            InputShellState.UNFOCUSED
                        }
                    InputPositiveIntegerOrZero(
                        modifier = Modifier.testTag(TEST_TAG_SYNC_PARAMETERS_EVENT_MAX_COUNT),
                        title = stringResource(R.string.events_to_download),
                        state = inputEventsToDownloadState,
                        inputTextFieldValue = eventsToDownload,
                        onValueChanged = { fieldValue ->
                            fieldValue?.let {
                                eventsToDownload = fieldValue
                            }
                            onEventToDownloadLimitUpdate(
                                fieldValue?.text?.toIntOrNull() ?: 0,
                            )
                        },
                        imeAction = ImeAction.Done,
                        onFocusChanged = { isFocused ->
                            inputEventsToDownloadState =
                                if (isFocused) InputShellState.FOCUSED else InputShellState.UNFOCUSED
                        },
                    )

                    var numTeisDownloadTextFieldValue =
                        remember {
                            TextFieldValue(
                                text = syncParametersViewModel.numberOfTeiToDownload.toString(),
                                selection = TextRange(syncParametersViewModel.numberOfTeiToDownload.toString().length),
                            )
                        }
                    var inputTeisToDownloadState =
                        remember {
                            InputShellState.UNFOCUSED
                        }
                    InputPositiveIntegerOrZero(
                        modifier = Modifier.testTag(TEST_TAG_SYNC_PARAMETERS_TEI_MAX_COUNT),
                        title = stringResource(R.string.teis_to_download),
                        state = inputTeisToDownloadState,
                        inputTextFieldValue = numTeisDownloadTextFieldValue,
                        onValueChanged = { fieldValue ->
                            fieldValue?.let {
                                numTeisDownloadTextFieldValue = fieldValue
                            }
                            onTeiToDownloadLimitUpdate(
                                fieldValue?.text?.toIntOrNull() ?: 0,
                            )
                        },
                        imeAction = ImeAction.Done,
                        onFocusChanged = { isFocused ->
                            inputTeisToDownloadState =
                                if (isFocused) InputShellState.FOCUSED else InputShellState.UNFOCUSED
                        },
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sync_parameters_not_editable),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                syncParametersViewModel.hasSpecificProgramSettings
                    .takeIf { it > 0 }
                    ?.let { specificSettings ->
                        Text(
                            text =
                                buildAnnotatedString {
                                    val specificProgramText =
                                        LocalContext.current.resources
                                            .getQuantityString(
                                                R.plurals.settings_specific_programs,
                                                syncParametersViewModel.hasSpecificProgramSettings,
                                            ).format(specificSettings)
                                    append(
                                        LocalContext.current.resources
                                            .getQuantityString(
                                                R.plurals.settings_specific_programs,
                                                syncParametersViewModel.hasSpecificProgramSettings,
                                            ).format(specificSettings),
                                    )
                                    val indexOfNumber =
                                        specificProgramText.indexOf(specificSettings.toString())
                                    addStyle(
                                        style =
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.primary,
                                                textDecoration = TextDecoration.None,
                                            ),
                                        start = indexOfNumber,
                                        end = indexOfNumber + indexOfNumber.toString().length,
                                    )
                                },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.program_settings),
                            style = ButtonStyle.TONAL,
                            enabled = true,
                            onClick = onSpecificProgramSettingsClick,
                        )
                    }
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun provideInfoItems(syncParametersViewModel: SyncParametersViewModel): List<AdditionalInfoItem> =
    listOf(
        AdditionalInfoItem(
            value =
                stringResource(R.string.limit_setting).format(
                    provideLimitScopeLabel(
                        syncParametersViewModel.limitScope,
                    ),
                ),
            isConstantItem = true,
        ),
        AdditionalInfoItem(
            key = stringResource(R.string.sync_events),
            value =
                syncParametersViewModel.currentEventCount.toString() +
                    "/" +
                    syncParametersViewModel.numberOfEventsToDownload.toString(),
            isConstantItem = true,
        ),
        AdditionalInfoItem(
            key = stringResource(R.string.tei),
            value =
                syncParametersViewModel.currentTeiCount.toString() +
                    "/" +
                    syncParametersViewModel.numberOfTeiToDownload.toString(),
            isConstantItem = true,
        ),
    )

@Composable
private fun provideLimitScopeLabel(limitScope: LimitScope) =
    when (limitScope) {
        LimitScope.ALL_ORG_UNITS,
        LimitScope.GLOBAL,
        -> stringResource(R.string.settings_limit_globally)

        LimitScope.PER_ORG_UNIT -> stringResource(R.string.settings_limit_ou)
        LimitScope.PER_PROGRAM -> stringResource(R.string.settings_limit_program)
        LimitScope.PER_OU_AND_PROGRAM -> stringResource(R.string.settings_limit_ou_program)
    }
