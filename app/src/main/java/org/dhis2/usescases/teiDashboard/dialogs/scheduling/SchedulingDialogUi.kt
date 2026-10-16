package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import org.dhis2.R
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatComboUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventInputDateUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvideCategorySelector
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvideInputDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvidePeriodSelector
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.willShowCalendar
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialog.LaunchMode
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesNoFieldValues
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.resource.provideStringResource
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import java.util.Locale

@Composable
fun SchedulingDialogUi(
    viewModel: SchedulingViewModel,
    launchMode: LaunchMode,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    val yesNoOptions =
        InputYesNoFieldValues.entries.map {
            RadioButtonData(
                it.value,
                selected = false,
                enabled = true,
                textInput = provideStringResource(it.value.lowercase(Locale.getDefault())),
            )
        }
    var optionSelected by remember { mutableStateOf(yesNoOptions.first()) }
    val scheduleNew by remember(optionSelected) {
        derivedStateOf { optionSelected == yesNoOptions.first() }
    }
    val bottomSheetTitle =
        bottomSheetTitle(
            launchMode = launchMode,
            programStages = uiState.programStages,
        )
    BottomSheetShell(
        uiState =
            BottomSheetShellUIState(
                showTopSectionDivider = false,
                showBottomSectionDivider = false,
                title = bottomSheetTitle,
                subtitle = uiState.overdueEventSubtitle,
                headerTextAlignment = TextAlign.Start,
                animateHeaderOnKeyboardAppearance = false,
            ),
        buttonBlock = {
            ButtonBlock(
                launchMode = launchMode,
                scheduleNew = scheduleNew,
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = onDismiss,
            )
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(Spacing.Spacing0),
                verticalArrangement = Arrangement.spacedBy(Spacing.Spacing16),
            ) {
                if (launchMode.showYesNoOptions) {
                    RadioButtonBlock(
                        modifier =
                            Modifier
                                .padding(bottom = Spacing.Spacing8)
                                .semantics { testTag = "YES_NO_OPTIONS" },
                        orientation = Orientation.HORIZONTAL,
                        content = yesNoOptions,
                        itemSelected = optionSelected,
                        onItemChange = {
                            optionSelected = it
                        },
                    )
                }

                if (scheduleNew) {
                    ProvideScheduleNewEventForm(
                        uiState = uiState,
                        viewModel = viewModel,
                        launchMode = launchMode,
                    )
                }
            }
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun ButtonBlock(
    launchMode: LaunchMode,
    scheduleNew: Boolean,
    uiState: SchedulingUiState,
    viewModel: SchedulingViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .padding(BottomSheetShellDefaults.buttonBlockPaddings()),
    ) {
        when (launchMode) {
            is LaunchMode.NewSchedule -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    style = ButtonStyle.FILLED,
                    enabled =
                        !scheduleNew ||
                            uiState.eventDate.isValid &&
                            uiState.eventCatCombo.isCompleted,
                    text = buttonTitle(scheduleNew),
                    onClick = {
                        when {
                            scheduleNew -> viewModel.scheduleEvent(launchMode)
                            else -> onDismiss()
                        }
                    },
                )
            }

            is LaunchMode.EnterEvent -> {
                Column(
                    modifier = Modifier.padding(Spacing.Spacing0),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
                ) {
                    val eventLabel =
                        uiState.programStage?.displayEventLabel() ?: stringResource(R.string.event)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.FILLED,
                        enabled = uiState.eventDate.isValid,
                        text = stringResource(R.string.enter_event, eventLabel),
                        onClick = {
                            viewModel.enterEvent(launchMode)
                        },
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.OUTLINED,
                        colorStyle = ColorStyle.WARNING,
                        text = stringResource(R.string.cancel_event, eventLabel),
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.EventBusy,
                                contentDescription = null,
                                tint = TextColor.OnWarningContainer,
                            )
                        },
                        onClick = {
                            viewModel.onCancelEvent()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun bottomSheetTitle(
    launchMode: LaunchMode,
    programStages: List<ProgramStage>,
): String {
    val prefix =
        when (launchMode) {
            is LaunchMode.NewSchedule -> stringResource(id = R.string.schedule_next)
            is LaunchMode.EnterEvent -> stringResource(id = R.string.scheduled_enter_event)
        }
    val defaultEventName = stringResource(id = R.string.event)
    val programName =
        when (programStages.size) {
            1 -> programStages.first().displayEventLabel() ?: defaultEventName
            else -> defaultEventName
        }
    val terminalSymbol =
        when (launchMode) {
            is LaunchMode.NewSchedule -> "?"
            is LaunchMode.EnterEvent -> ""
        }

    return "$prefix $programName$terminalSymbol"
}

@Composable
fun buttonTitle(scheduleNew: Boolean): String =
    when (scheduleNew) {
        true -> stringResource(id = R.string.schedule)
        false -> stringResource(id = R.string.done)
    }

@Composable
fun ProvideScheduleNewEventForm(
    uiState: SchedulingUiState,
    viewModel: SchedulingViewModel,
    launchMode: LaunchMode,
) {
    if (uiState.programStages.size > 1 && launchMode !is LaunchMode.EnterEvent) {
        var dropdownItems by remember {
            mutableStateOf(
                uiState.programStages.map { DropdownItem(it.displayName().orEmpty()) },
            )
        }
        InputDropDown(
            title = uiState.programStageLabel ?: "",
            state = InputShellState.UNFOCUSED,
            fetchItem = { index -> dropdownItems[index] },
            itemCount = dropdownItems.size,
            onSearchOption = { query ->
                dropdownItems =
                    if (query.isNotEmpty()) {
                        dropdownItems.filter { it.label.contains(query) }
                    } else {
                        uiState.programStages.map { DropdownItem(it.displayName().orEmpty()) }
                    }
            },
            useDropDown = dropdownItems.size < 15,
            loadOptions = {
                // no-op
            },
            selectedItem = DropdownItem(uiState.programStage?.displayName().orEmpty()),
            onResetButtonClicked = {},
            onItemSelected = { index, _ ->
                uiState.programStages[index].let { viewModel.updateStage(it) }
            },
        )
    }

    if (willShowCalendar(uiState.programStage?.periodType())) {
        ProvideInputDate(
            EventInputDateUiModel(
                eventDate = uiState.eventDate,
                detailsEnabled = true,
                selectableDates = viewModel.getSelectableDates(),
                onDateClick = {},
                onDateSelected = { viewModel.onDateSet(it.year, it.month, it.day) },
                onClear = { viewModel.onClearEventReportDate() },
                onError = { viewModel.onDateError() },
            ),
        )
    } else {
        ProvidePeriodSelector(
            uiModel =
                EventInputDateUiModel(
                    eventDate = uiState.eventDate,
                    detailsEnabled = true,
                    onDateClick = { viewModel.showPeriodDialog() },
                    onDateSelected = {},
                    onClear = { viewModel.onClearEventReportDate() },
                    required = true,
                    showField = uiState.eventDate.active,
                    selectableDates = viewModel.getSelectableDates(),
                ),
            modifier = Modifier,
        )
    }

    if (!uiState.eventCatCombo.isDefault && launchMode !is LaunchMode.EnterEvent) {
        uiState.eventCatCombo.categories.forEach { category ->

            ProvideCategorySelector(
                eventCatComboUiModel =
                    EventCatComboUiModel(
                        category = category,
                        eventCatCombo = uiState.eventCatCombo,
                        detailsEnabled = true,
                        currentDate = uiState.eventDate.currentDate,
                        selectedOrgUnit = uiState.enrollment?.organisationUnit,
                        onClearCatCombo = { viewModel.onClearCatCombo() },
                        onOptionSelected = {
                            val selectedOption = Pair(category.uid, it?.uid())
                            viewModel.setUpCategoryCombo(selectedOption)
                        },
                        required = true,
                        noOptionsText = stringResource(R.string.no_options),
                        catComboText = stringResource(R.string.cat_combo),
                    ),
            )
        }
    }
}
