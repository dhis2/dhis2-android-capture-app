package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SchedulingViewModel(
    val d2: D2,
    val resourceManager: ResourceManager,
    val periodUtils: DhisPeriodUtils,
) : ViewModel() {

    lateinit var repository: EventDetailsRepository
    lateinit var configureEventReportDate: ConfigureEventReportDate
    lateinit var configureEventCatCombo: ConfigureEventCatCombo

    lateinit var enrollment: Enrollment
    lateinit var programStages: List<ProgramStage>

    private val _programStage: MutableStateFlow<ProgramStage?> = MutableStateFlow(null)
    val programStage: StateFlow<ProgramStage?> get() = _programStage

    var showCalendar: (() -> Unit)? = null
    var showPeriods: (() -> Unit)? = null
    var onEventScheduled: ((String) -> Unit)? = null

    private val _eventDate: MutableStateFlow<EventDate> = MutableStateFlow(EventDate())
    val eventDate: StateFlow<EventDate> get() = _eventDate

    private val _eventCatCombo: MutableStateFlow<EventCatCombo> = MutableStateFlow(EventCatCombo())
    val eventCatCombo: StateFlow<EventCatCombo> get() = _eventCatCombo

    private fun loadConfiguration() {
        repository = EventDetailsRepository(
            d2 = d2,
            programUid = enrollment.program().orEmpty(),
            eventUid = null,
            programStageUid = programStage.value?.uid(),
            fieldFactory = null,
            eventCreationType = EventCreationType.SCHEDULE,
            onError = resourceManager::parseD2Error,
        )
        configureEventReportDate = ConfigureEventReportDate(
            creationType = EventCreationType.SCHEDULE,
            resourceProvider = EventDetailResourcesProvider(
                enrollment.program().orEmpty(),
                programStage.value?.uid(),
                resourceManager,
            ),
            repository = repository,
            periodType = programStage.value?.periodType(),
            periodUtils = periodUtils,
            enrollmentId = enrollment.uid(),
            scheduleInterval = programStage.value?.standardInterval() ?: 0,
        )
        configureEventCatCombo = ConfigureEventCatCombo(
            repository = repository,
        )
        loadProgramStage()
    }

    private fun loadProgramStage() {
        viewModelScope.launch {
            configureEventReportDate().collect {
                _eventDate.value = it
            }

            configureEventCatCombo()
                .collect {
                    _eventCatCombo.value = it
                }
        }
    }

    fun getSelectableDates(): SelectableDates {
        val maxDate = if (!eventDate.value.allowFutureDates) {
            SimpleDateFormat("ddMMyyyy", Locale.US).format(Date(System.currentTimeMillis() - 1000))
        } else if (eventDate.value.maxDate != null) {
            SimpleDateFormat("ddMMyyyy", Locale.US).format(eventDate.value.maxDate)
        } else {
            "12112124"
        }
        val minDate = if (eventDate.value.minDate != null) {
            SimpleDateFormat("ddMMyyyy", Locale.US).format(eventDate.value.minDate)
        } else {
            "12111924"
        }

        return SelectableDates(minDate, maxDate)
    }

    fun setUpEventReportDate(selectedDate: Date? = null) {
        viewModelScope.launch {
            configureEventReportDate(selectedDate)
                .flowOn(Dispatchers.IO)
                .collect {
                    _eventDate.value = it
                }
        }
    }

    fun onClearEventReportDate() {
        _eventDate.value = eventDate.value.copy(currentDate = null)
    }

    fun setUpCategoryCombo(categoryOption: Pair<String, String?>? = null) {
        viewModelScope.launch {
            configureEventCatCombo(categoryOption)
                .flowOn(Dispatchers.IO)
                .collect {
                    _eventCatCombo.value = it
                }
        }
    }

    fun onClearCatCombo() {
        _eventCatCombo.value = eventCatCombo.value.copy(isCompleted = false)
    }

    fun showPeriodDialog() {
        programStage.value?.periodType()?.let {
            showPeriods?.invoke()
        }
    }

    fun onDateSet(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar[year, month - 1, day, 0, 0] = 0
        calendar[Calendar.MILLISECOND] = 0
        val selectedDate = calendar.time
        setUpEventReportDate(selectedDate)
    }

    fun updateStage(stage: ProgramStage) {
        _programStage.value = stage
        loadConfiguration()
    }

    fun scheduleEvent() {
        viewModelScope.launch {
            eventDate.value.currentDate?.let { date ->
                repository.scheduleEvent(
                    enrollmentUid = enrollment.uid(),
                    dueDate = date,
                    orgUnitUid = enrollment.organisationUnit(),
                    categoryOptionComboUid = eventCatCombo.value.uid,
                ).flowOn(Dispatchers.IO)
                    .collect {
                        if (it != null) {
                            onEventScheduled?.invoke(programStage.value?.uid() ?: "")
                        }
                    }
            }
        }
    }

    fun setInitialProgramStage(programStage: ProgramStage) {
        _programStage.value = programStage
        loadConfiguration()
    }
}
