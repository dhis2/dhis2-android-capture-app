package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.programStage
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.date.toOverdueOrScheduledUiText
import org.dhis2.commons.periods.domain.GetEventPeriods
import org.dhis2.commons.periods.model.Period
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialog.LaunchMode
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SchedulingViewModel(
    private val d2: D2,
    private val resourceManager: ResourceManager,
    private val eventResourcesProvider: EventResourcesProvider,
    private val periodUtils: DhisPeriodUtils,
    private val dispatchersProvider: DispatcherProvider,
    private val launchMode: LaunchMode,
    private val dateUtils: DateUtils,
    private val getEventPeriods: GetEventPeriods,
) : ViewModel() {
    lateinit var repository: EventDetailsRepository
    lateinit var configureEventReportDate: ConfigureEventReportDate
    lateinit var configureEventCatCombo: ConfigureEventCatCombo

    var showCalendar: (() -> Unit)? = null
    var showPeriods: ((periodType: PeriodType) -> Unit)? = null
    var onEventScheduled: ((String) -> Unit)? = null
    var onEventSkipped: ((String?) -> Unit)? = null
    var onDueDateUpdated: (() -> Unit)? = null
    var onEnterEvent: ((String, String) -> Unit)? = null

    private val _eventDate: MutableStateFlow<EventDate> = MutableStateFlow(EventDate())
    val eventDate: StateFlow<EventDate> = _eventDate

    private val _eventCatCombo: MutableStateFlow<EventCatCombo> = MutableStateFlow(EventCatCombo())
    val eventCatCombo: StateFlow<EventCatCombo> = _eventCatCombo

    private val _programStage: MutableStateFlow<ProgramStage?> = MutableStateFlow(null)
    val programStage: StateFlow<ProgramStage?> = _programStage

    private val _programStages: MutableStateFlow<List<ProgramStage>> = MutableStateFlow(emptyList())
    val programStages: StateFlow<List<ProgramStage>> = _programStages

    private val _enrollment: MutableStateFlow<Enrollment?> = MutableStateFlow(null)
    val enrollment: StateFlow<Enrollment?> = _enrollment

    private val _overdueEventSubtitle: MutableStateFlow<String?> = MutableStateFlow(null)
    val overdueEventSubtitle: StateFlow<String?> = _overdueEventSubtitle

    init {
        viewModelScope.launch {
            val enrollment =
                withContext(dispatchersProvider.io()) {
                    when (launchMode) {
                        is LaunchMode.NewSchedule -> d2.enrollment(launchMode.enrollmentUid)
                        is LaunchMode.EnterEvent -> {
                            val enrollmentUid = d2.event(launchMode.eventUid)?.enrollment()
                            enrollmentUid?.let { d2.enrollment(it) }
                        }
                    }
                }
            _enrollment.value = enrollment

            val programStages =
                withContext(dispatchersProvider.io()) {
                    when (launchMode) {
                        is LaunchMode.NewSchedule -> {
                            launchMode.programStagesUids.mapNotNull(d2::programStage)
                        }

                        is LaunchMode.EnterEvent -> emptyList()
                    }
                }
            _programStages.value = programStages

            val programStage =
                withContext(dispatchersProvider.io()) {
                    when (launchMode) {
                        is LaunchMode.NewSchedule -> programStages.first()
                        is LaunchMode.EnterEvent -> {
                            val eventProgramStageId = d2.event(launchMode.eventUid)?.programStage()
                            d2
                                .programModule()
                                .programStages()
                                .uid(eventProgramStageId)
                                .blockingGet()
                        }
                    }
                }
            _programStage.value = programStage

            loadScheduleConfiguration(launchMode)
        }
    }

    private fun loadScheduleConfiguration(launchMode: LaunchMode) {
        val enrollment = enrollment.value
        val event =
            when (launchMode) {
                is LaunchMode.EnterEvent -> d2.event(launchMode.eventUid)
                is LaunchMode.NewSchedule -> null
            }
        val programId =
            when (launchMode) {
                is LaunchMode.NewSchedule -> enrollment?.program()
                is LaunchMode.EnterEvent -> event?.program()
            }.orEmpty()
        val enrollmentId =
            when (launchMode) {
                is LaunchMode.NewSchedule -> enrollment?.uid()
                is LaunchMode.EnterEvent -> event?.enrollment().orEmpty()
            }

        repository =
            EventDetailsRepository(
                d2 = d2,
                programUid = programId,
                eventUid = event?.uid(),
                programStageUid = programStage.value?.uid(),
                fieldFactory = null,
                eventCreationType = launchMode.eventCreationType,
                onError = resourceManager::parseD2Error,
            )
        configureEventReportDate =
            ConfigureEventReportDate(
                creationType = launchMode.eventCreationType,
                resourceProvider = eventDetailResourcesProvider(programId),
                repository = repository,
                periodType = programStage.value?.periodType(),
                periodUtils = periodUtils,
                enrollmentId = enrollmentId,
                scheduleInterval = programStage.value?.standardInterval() ?: 0,
            )
        configureEventCatCombo = ConfigureEventCatCombo(repository = repository)

        loadProgramStage(event = event)
    }

    private fun eventDetailResourcesProvider(programId: String) =
        EventDetailResourcesProvider(
            programUid = programId,
            programStage = programStage.value?.uid(),
            resourceManager = resourceManager,
            eventResourcesProvider = eventResourcesProvider,
        )

    private fun loadProgramStage(event: Event? = null) {
        viewModelScope.launch {
            val selectedDate = event?.dueDate() ?: configureEventReportDate.getNextScheduleDate()
            configureEventReportDate(selectedDate = selectedDate).collect {
                _eventDate.value = it
            }

            _overdueEventSubtitle.value = getOverdueSubtitle()

            configureEventCatCombo().collect {
                _eventCatCombo.value = it
            }
        }
    }

    fun getSelectableDates(): SelectableDates {
        val maxDate =
            if (!eventDate.value.allowFutureDates) {
                SimpleDateFormat("ddMMyyyy", Locale.US).format(Date(System.currentTimeMillis() - 1000))
            } else if (eventDate.value.maxDate != null) {
                SimpleDateFormat("ddMMyyyy", Locale.US).format(eventDate.value.maxDate)
            } else {
                "12112124"
            }
        val minDate =
            if (eventDate.value.minDate != null) {
                SimpleDateFormat("ddMMyyyy", Locale.US).format(eventDate.value.minDate)
            } else {
                "12111924"
            }

        return SelectableDates(minDate, maxDate)
    }

    fun setUpEventReportDate(selectedDate: Date? = null) {
        viewModelScope.launch {
            configureEventReportDate(selectedDate)
                .flowOn(dispatchersProvider.io())
                .collect {
                    _eventDate.value = it

                    if (launchMode is LaunchMode.EnterEvent) {
                        updateEventDueDate(
                            eventUid = launchMode.eventUid,
                            dueDate = it,
                        )
                    }
                }
        }
    }

    private fun updateEventDueDate(
        eventUid: String,
        dueDate: EventDate,
    ) {
        viewModelScope.launch {
            launch(dispatchersProvider.io()) {
                d2.eventModule().events().uid(eventUid).run {
                    setDueDate(dueDate.currentDate)
                    setStatus(EventStatus.SCHEDULE)
                    onDueDateUpdated?.invoke()
                }
            }
        }
    }

    fun onClearEventReportDate() {
        _eventDate.value =
            eventDate.value.copy(
                currentDate = null,
                dateValue = null,
            )
    }

    fun onDateError() {
        _eventDate.update {
            it.copy(error = true)
        }
    }

    fun setUpCategoryCombo(categoryOption: Pair<String, String?>? = null) {
        viewModelScope.launch {
            configureEventCatCombo(categoryOption)
                .flowOn(dispatchersProvider.io())
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
            showPeriods?.invoke(it)
        }
    }

    fun onDateSet(
        year: Int,
        month: Int,
        day: Int,
    ) {
        val calendar = Calendar.getInstance()
        calendar[year, month - 1, day, 0, 0] = 0
        calendar[Calendar.MILLISECOND] = 0
        val selectedDate = calendar.time
        setUpEventReportDate(selectedDate)
    }

    fun updateStage(stage: ProgramStage) {
        _programStage.value = stage
        loadScheduleConfiguration(launchMode = launchMode)
    }

    fun scheduleEvent(launchMode: LaunchMode.NewSchedule) {
        viewModelScope.launch {
            val eventDate = eventDate.value.currentDate ?: return@launch
            val enrollment = enrollment.value ?: return@launch
            val orgUnitUid = launchMode.ownerOrgUnitUid ?: enrollment.organisationUnit()
            repository
                .scheduleEvent(
                    enrollmentUid = enrollment.uid(),
                    dueDate = eventDate,
                    orgUnitUid = orgUnitUid,
                    categoryOptionComboUid = eventCatCombo.value.uid,
                ).flowOn(dispatchersProvider.io())
                .collect {
                    if (it != null) {
                        onEventScheduled?.invoke(programStage.value?.uid() ?: "")
                    }
                }
        }
    }

    fun enterEvent(launchMode: LaunchMode.EnterEvent) {
        viewModelScope.launch {
            val event =
                withContext(dispatchersProvider.io()) {
                    d2.event(launchMode.eventUid)
                } ?: return@launch
            val programUid = event.program() ?: return@launch

            d2.eventModule().events().uid(launchMode.eventUid).run {
                setEventDate(dateUtils.getStartOfDay(Date()))
                setStatus(EventStatus.ACTIVE)
            }

            onEnterEvent?.invoke(
                launchMode.eventUid,
                programUid,
            )
        }
    }

    fun onCancelEvent() {
        viewModelScope.launch {
            when (launchMode) {
                is LaunchMode.EnterEvent -> {
                    d2
                        .eventModule()
                        .events()
                        .uid(launchMode.eventUid)
                        .setStatus(EventStatus.SKIPPED)
                    onEventSkipped?.invoke(programStage.value?.displayEventLabel())
                }

                is LaunchMode.NewSchedule -> {
                    // no-op
                }
            }
        }
    }

    fun fetchPeriods(): Flow<PagingData<Period>> {
        val programStage = programStage.value ?: return emptyFlow()
        val periodType = programStage.periodType() ?: PeriodType.Daily
        val enrollmentUid = enrollment.value?.uid() ?: return emptyFlow()
        return getEventPeriods(
            eventUid = null,
            periodType = periodType,
            selectedDate = eventDate.value.currentDate,
            programStage = programStage,
            isScheduling = true,
            eventEnrollmentUid = enrollmentUid,
        )
    }

    private fun getOverdueSubtitle(): String? {
        return if (launchMode is LaunchMode.NewSchedule) {
            null
        } else {
            val eventDate = _eventDate.value.currentDate ?: return null
            eventDate.toOverdueOrScheduledUiText(
                resourceManager = resourceManager,
                isScheduling = true,
            )
        }
    }
}
