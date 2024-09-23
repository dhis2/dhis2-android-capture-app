package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatComboUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventInputDateUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvideCategorySelector
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvideInputDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvidePeriodSelector
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.willShowCalendar
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesNoFieldValues
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData
import org.hisp.dhis.mobile.ui.designsystem.resource.provideStringResource
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@Composable
fun SchedulingDialogUi(
    programStages: List<ProgramStage>,
    viewModel: SchedulingViewModel,
    orgUnitUid: String?,
    onDismiss: () -> Unit,
) {
    val date by viewModel.eventDate.collectAsState()
    val catCombo by viewModel.eventCatCombo.collectAsState()
    val selectedProgramStage by viewModel.programStage.collectAsState()

    val yesNoOptions = InputYesNoFieldValues.entries.map {
        RadioButtonData(
            it.value,
            selected = false,
            enabled = true,
            textInput = provideStringResource(it.value),
        )
    }
    var optionSelected by remember { mutableStateOf(yesNoOptions.first()) }
    val scheduleNew by remember(optionSelected) {
        derivedStateOf { optionSelected == yesNoOptions.first() }
    }

    val onButtonClick = {
        when {
            scheduleNew -> viewModel.scheduleEvent()
            else -> onDismiss()
        }
    }
    BottomSheetShell(
        title = bottomSheetTitle(programStages),
        buttonBlock = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                style = ButtonStyle.FILLED,
                enabled = !scheduleNew ||
                    !date.dateValue.isNullOrEmpty() &&
                    catCombo.isCompleted,
                text = buttonTitle(scheduleNew),
                onClick = onButtonClick,
            )
        },
        showSectionDivider = false,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButtonBlock(
                    modifier = Modifier.padding(bottom = Spacing.Spacing8),
                    orientation = Orientation.HORIZONTAL,
                    content = yesNoOptions,
                    itemSelected = optionSelected,
                    onItemChange = {
                        optionSelected = it
                    },
                )
                if (scheduleNew) {
                    ProvideScheduleNewEventForm(
                        programStages = programStages,
                        viewModel = viewModel,
                        selectedProgramStage = selectedProgramStage,
                        date = date,
                        catCombo = catCombo,
                        orgUnitUid = orgUnitUid,
                    )
                }
            }
        },
        onDismiss = onDismiss,
    )
}

@Composable
fun bottomSheetTitle(programStages: List<ProgramStage>): String =
    stringResource(id = R.string.schedule_next) + " " +
        when (programStages.size) {
            1 -> programStages.first().displayName()
            else -> stringResource(id = R.string.event)
        } + "?"

@Composable
fun buttonTitle(scheduleNew: Boolean): String = when (scheduleNew) {
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
) {
    if (programStages.size > 1) {
        InputDropDown(
            title = stringResource(id = R.string.program_stage),
            state = InputShellState.UNFOCUSED,
            dropdownItems = programStages.map { DropdownItem(it.displayName().orEmpty()) },
            selectedItem = DropdownItem(selectedProgramStage?.displayName().orEmpty()),
            onResetButtonClicked = {},
            onItemSelected = { item ->
                programStages.find { it.displayName() == item.label }
                    ?.let { viewModel.updateStage(it) }
            },
        )
    }

    if (willShowCalendar(selectedProgramStage?.periodType())) {
        ProvideInputDate(
            EventInputDateUiModel(
                eventDate = date,
                detailsEnabled = true,
                onDateClick = {},
                onDateSelected = { viewModel.onDateSet(it.year, it.month, it.day) },
                onClear = { viewModel.onClearEventReportDate() },
            ),
        )
    } else {
        ProvidePeriodSelector(
            uiModel = EventInputDateUiModel(
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

    if (!catCombo.isDefault) {
        catCombo.categories.forEach { category ->
            ProvideCategorySelector(
                eventCatComboUiModel = EventCatComboUiModel(
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
