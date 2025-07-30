package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.usescases.settings.SettingItem
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.hisp.dhis.android.core.settings.LimitScope
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
    SettingItem(
        modifier = Modifier.testTag(SettingItem.SYNC_PARAMETERS.name),
        title = stringResource(id = R.string.settingsSyncParameters),
        subtitle = stringResource(R.string.event_tei_limits_v2).format(
            provideLimitScopeLabel(syncParametersViewModel.limitScope),
            syncParametersViewModel.currentEventCount,
            syncParametersViewModel.numberOfEventsToDownload,
            syncParametersViewModel.currentTeiCount,
            syncParametersViewModel.numberOfTeiToDownload,
        ),
        icon = Icons.Outlined.DataUsage,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (syncParametersViewModel.limitScopeIsEditable) {
                    val downloadLimitScopes = listOf(
                        stringResource(R.string.settings_limit_globally),
                        stringResource(R.string.settings_limit_ou),
                        stringResource(R.string.settings_limit_program),
                        stringResource(R.string.settings_limit_ou_program),
                    )
                    InputDropDown(
                        modifier = Modifier.testTag(TestTag_SyncParameters_LimitScope),
                        title = stringResource(R.string.settings_limit_scope),
                        state = InputShellState.FOCUSED,
                        itemCount = downloadLimitScopes.size,
                        onSearchOption = {},
                        fetchItem = { index ->
                            DropdownItem(downloadLimitScopes[index])
                        },
                        selectedItem = DropdownItem(
                            label = when (syncParametersViewModel.limitScope) {
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
                            val selectedScope = when (index) {
                                0 -> LimitScope.GLOBAL
                                1 -> LimitScope.PER_ORG_UNIT
                                2 -> LimitScope.PER_PROGRAM
                                3 -> LimitScope.PER_OU_AND_PROGRAM
                                else -> LimitScope.GLOBAL
                            }
                            onScopeLimitSelected(selectedScope)
                        },
                        loadOptions = {},
                    )

                    InputPositiveIntegerOrZero(
                        modifier = Modifier.testTag(TestTag_SyncParameters_EventMaxCount),
                        title = stringResource(R.string.events_to_download),
                        state = InputShellState.FOCUSED,
                        inputTextFieldValue = TextFieldValue(text = syncParametersViewModel.numberOfEventsToDownload.toString()),
                        onValueChanged = { fieldValue ->
                            onEventToDownloadLimitUpdate(
                                fieldValue?.text?.toIntOrNull() ?: 0,
                            )
                        },
                        imeAction = ImeAction.Done,
                    )

                    InputPositiveIntegerOrZero(
                        modifier = Modifier.testTag(TestTag_SyncParameters_TeiMaxCount),
                        title = stringResource(R.string.teis_to_download),
                        state = InputShellState.FOCUSED,
                        inputTextFieldValue = TextFieldValue(text = syncParametersViewModel.numberOfTeiToDownload.toString()),
                        onValueChanged = { fieldValue ->
                            onTeiToDownloadLimitUpdate(
                                fieldValue?.text?.toIntOrNull() ?: 0,
                            )
                        },
                        imeAction = ImeAction.Done,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sync_parameters_not_editable),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                syncParametersViewModel.hasSpecificProgramSettings.takeIf { it > 0 }
                    ?.let { specificSettings ->
                        Text(
                            text = buildAnnotatedString {
                                val specificProgramText =
                                    LocalContext.current.resources.getQuantityString(
                                        R.plurals.settings_specific_programs,
                                        syncParametersViewModel.hasSpecificProgramSettings,
                                    ).format(specificSettings)
                                append(
                                    LocalContext.current.resources.getQuantityString(
                                        R.plurals.settings_specific_programs,
                                        syncParametersViewModel.hasSpecificProgramSettings,
                                    ).format(specificSettings),
                                )
                                val indexOfNumber =
                                    specificProgramText.indexOf(specificSettings.toString())
                                addStyle(
                                    style = SpanStyle(
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
private fun provideLimitScopeLabel(limitScope: LimitScope) = when (limitScope) {
    LimitScope.ALL_ORG_UNITS,
    LimitScope.GLOBAL,
    -> stringResource(R.string.settings_limit_globally)

    LimitScope.PER_ORG_UNIT -> stringResource(R.string.settings_limit_ou)
    LimitScope.PER_PROGRAM -> stringResource(R.string.settings_limit_program)
    LimitScope.PER_OU_AND_PROGRAM -> stringResource(R.string.settings_limit_ou_program)
}
