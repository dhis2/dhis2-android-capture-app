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
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatComboUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
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
    val date by viewModel.eventDate.collectAsState()
    val catCombo by viewModel.eventCatCombo.collectAsState()
    val programStages by viewModel.programStages.collectAsState()
    val selectedProgramStage by viewModel.programStage.collectAsState()
    val enrollment by viewModel.enrollment.collectAsState()
    val overdueSubtitle by viewModel.overdueEventSubtitle.collectAsState()

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
            programStages = programStages,
        )
    BottomSheetShell(
        uiState =
            BottomSheetShellUIState(
                showTopSectionDivider = false,
                showBottomSectionDivider = false,
                title = bottomSheetTitle,
                subtitle = overdueSubtitle,
                headerTextAlignment = TextAlign.Start,
                animateHeaderOnKeyboardAppearance = false,
            ),
        buttonBlock = {
            ButtonBlock(
                launchMode = launchMode,
                scheduleNew = scheduleNew,
                date = date,
                catCombo = catCombo,
                selectedProgramStage = selectedProgramStage,
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
                        programStages = programStages,
                        viewModel = viewModel,
                        selectedProgramStage = selectedProgramStage,
                        date = date,
                        catCombo = catCombo,
                        orgUnitUid = enrollment?.organisationUnit(),
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
    date: EventDate,
    catCombo: EventCatCombo,
    selectedProgramStage: ProgramStage?,
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
                            date.isValid &&
                            catCombo.isCompleted,
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
                        selectedProgramStage?.displayEventLabel() ?: stringResource(R.string.event)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.FILLED,
                        enabled = date.isValid,
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
    programStages: List<ProgramStage>,
    viewModel: SchedulingViewModel,
    selectedProgramStage: ProgramStage?,
    date: EventDate,
    catCombo: EventCatCombo,
    orgUnitUid: String?,
    launchMode: LaunchMode,
) {
    if (programStages.size > 1 && launchMode !is LaunchMode.EnterEvent) {
        var dropdownItems by remember {
            mutableStateOf(
                programStages.map { DropdownItem(it.displayName().orEmpty()) },
            )
        }
        InputDropDown(
            title = stringResource(id = R.string.program_stage),
            state = InputShellState.UNFOCUSED,
            fetchItem = { index -> dropdownItems[index] },
            itemCount = dropdownItems.size,
            onSearchOption = { query ->
                dropdownItems =
                    if (query.isNotEmpty()) {
                        dropdownItems.filter { it.label.contains(query) }
                    } else {
                        programStages.map { DropdownItem(it.displayName().orEmpty()) }
                    }
            },
            useDropDown = dropdownItems.size < 15,
            loadOptions = {
                // no-op
            },
            selectedItem = DropdownItem(selectedProgramStage?.displayName().orEmpty()),
            onResetButtonClicked = {},
            onItemSelected = { index, _ ->
                programStages[index].let { viewModel.updateStage(it) }
            },
        )
    }

    if (willShowCalendar(selectedProgramStage?.periodType())) {
        ProvideInputDate(
            EventInputDateUiModel(
                eventDate = date,
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
                    eventDate = date,
                    detailsEnabled = true,
                    onDateClick = { viewModel.showPeriodDialog() },
                    onDateSelected = {},
                    onClear = { viewModel.onClearEventReportDate() },
                    required = true,
                    showField = date.active,
                    selectableDates = viewModel.getSelectableDates(),
                ),
            modifier = Modifier,
        )
    }

    if (!catCombo.isDefault && launchMode !is LaunchMode.EnterEvent) {
        catCombo.categories.forEach { category ->

            ProvideCategorySelector(
                eventCatComboUiModel =
                    EventCatComboUiModel(
                        category = category,
                        eventCatCombo = catCombo,
                        detailsEnabled = true,
                        currentDate = date.currentDate,
                        selectedOrgUnit = orgUnitUid,
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
