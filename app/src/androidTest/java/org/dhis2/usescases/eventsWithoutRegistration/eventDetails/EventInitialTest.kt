package org.dhis2.usescases.eventsWithoutRegistration.eventDetails

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.GeometryParserImpl
import org.dhis2.form.model.FieldUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.CreateOrUpdateEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatComboUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventInputDateUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EMPTY_CATEGORY_SELECTOR
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.INPUT_EVENT_INITIAL_DATE
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvideCategorySelector
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvideInputDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui.EventDetailsViewModel
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.text.SimpleDateFormat
import java.util.Date
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.ui.MetadataIconData
import org.mockito.kotlin.any

@ExperimentalCoroutinesApi
class EventInitialTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dateString = "Sun Aug 20 00:00:00 GMT+02:00 2023"

    private val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy")

    val date: Date? = dateFormat.parse(dateString)

    private val eventDetailsRepository: EventDetailsRepository = mock {
        on { getProgramStage() } doReturn programStage
        on { catCombo() } doReturn catCombo
        on { getEvent() } doReturn null
        on { getObjectStyle() } doReturn style
        on { getOrganisationUnit(ORG_UNIT_UID) } doReturn orgUnit
        on { getGeometryModel() } doReturn geometryModel
        on { getCatOptionCombos(CAT_COMBO_UID) } doReturn listOf(categoryOptionCombo)
        on { getEditableStatus() } doReturn EventEditableStatus.Editable()
        on { getEnrollmentDate(ENROLLMENT_UID) } doReturn date
        on { getStageLastDate(ENROLLMENT_UID) } doReturn DateUtils.uiDateFormat()
            .parse("20/8/2023")!!

    }

    private val metadataIconProvider:MetadataIconProvider = mock{
        on { invoke(any(), any(), any()) }doReturn MetadataIconData.Resource(1,1)
    }

    private lateinit var viewModel: EventDetailsViewModel


    private val locationProvider: LocationProvider = mock()
    private val resourceManager: ResourceManager = mock()


    private val periodUtils: DhisPeriodUtils = mock()
    private val preferencesProvider: PreferenceProvider = mock()

    private val style: ObjectStyle = mock()

    private val programStage: ProgramStage = mock {
        on { displayName() } doReturn PROGRAM_STAGE_NAME
        on { executionDateLabel() } doReturn EXECUTION_DATE
        on { generatedByEnrollmentDate() } doReturn true
        on { uid() } doReturn "programStage"
    }
    private val catCombo: CategoryCombo = mock {
        on { uid() } doReturn CAT_COMBO_UID
        on { isDefault } doReturn true
    }
    private val categoryOptionCombo: CategoryOptionCombo = mock {
        on { uid() } doReturn CAT_OPTION_COMBO_UID
    }
    private val orgUnit: OrganisationUnit = mock {
        on { uid() } doReturn ORG_UNIT_UID
    }
    private val geometryModel: FieldUiModel = mock {
        on { value } doReturn COORDINATES
    }

    private val eventDetailResourcesProvider: EventDetailResourcesProvider = mock {
        on { provideDueDate() } doReturn "Due date"
    }

    private fun createConfigureEventTemp(eventCreationType: EventCreationType) = ConfigureEventTemp(
        creationType = eventCreationType,
    )

    private fun createConfigureEventCatCombo() = ConfigureEventCatCombo(
        repository = eventDetailsRepository,
    )

    private fun createConfigureEventCoordinates() = ConfigureEventCoordinates(
        repository = eventDetailsRepository,
    )

    private fun createConfigureOrgUnit(eventCreationType: EventCreationType) = ConfigureOrgUnit(
        creationType = eventCreationType,
        repository = eventDetailsRepository,
        preferencesProvider = preferencesProvider,
        programUid = PROGRAM_UID,
        initialOrgUnitUid = INITIAL_ORG_UNIT_UID,
    )

    private fun createConfigureEventReportDate(
        eventCreationType: EventCreationType,
        periodType: PeriodType?,
        sheduleInterval: Int = 0,
    ) = ConfigureEventReportDate(
        creationType = eventCreationType,
        resourceProvider = eventDetailResourcesProvider,
        repository = eventDetailsRepository,
        periodType = periodType,
        periodUtils = periodUtils,
        enrollmentId = ENROLLMENT_UID,
        scheduleInterval = sheduleInterval,
    )

    private fun createConfigureEventDetails(
        eventCreationType: EventCreationType,
        enrollmentStatus: EnrollmentStatus,
    ) = ConfigureEventDetails(
        repository = eventDetailsRepository,
        resourcesProvider = provideEventResourcesProvider(),
        creationType = eventCreationType,
        enrollmentStatus = enrollmentStatus,
        metadataIconProvider = metadataIconProvider
    )

    private fun provideEventResourcesProvider() = EventDetailResourcesProvider(PROGRAM_UID, programStage.uid(), resourceManager)

    private fun createOrUpdateEventDetails() = CreateOrUpdateEventDetails(
        repository = eventDetailsRepository,
        resourcesProvider = provideEventResourcesProvider(),
    )

    private fun createGeometryController() = GeometryController(GeometryParserImpl())

    companion object {
        const val ENROLLMENT_UID = "enrollmentUid"
        const val PROGRAM_UID = "programUid"
        const val EVENT_UID = "eventUid"
        const val INITIAL_ORG_UNIT_UID = "initialOrgUnitUid"
        const val PROGRAM_STAGE_NAME = "Marvellous Program Stage"
        const val EXECUTION_DATE = "Date of Marvellous Program Stage"
        const val ORG_UNIT_UID = "orgUnitUid"
        const val COORDINATES = "coordinates"
        const val CAT_COMBO_UID = "CatComboUid"
        const val CAT_OPTION_COMBO_UID = "CategoryOptionComboUid"
    }

    private fun initViewModel(
        periodType: PeriodType?,
        eventCreationType: EventCreationType = EventCreationType.DEFAULT,
        enrollmentStatus: EnrollmentStatus,
        scheduleInterval: Int = 0,
    ) = EventDetailsViewModel(
        configureEventDetails = createConfigureEventDetails(
            eventCreationType,
            enrollmentStatus,
        ),
        configureEventReportDate = createConfigureEventReportDate(
            eventCreationType,
            periodType,
            scheduleInterval,
        ),
        configureOrgUnit = createConfigureOrgUnit(eventCreationType),
        configureEventCoordinates = createConfigureEventCoordinates(),
        configureEventCatCombo = createConfigureEventCatCombo(),
        configureEventTemp = createConfigureEventTemp(eventCreationType),
        periodType = periodType,
        eventUid = EVENT_UID,
        geometryController = createGeometryController(),
        locationProvider = locationProvider,
        createOrUpdateEventDetails = createOrUpdateEventDetails(),
        resourcesProvider = provideEventResourcesProvider(),
    )


    @Test
    fun shouldAddStandardIntervalDaysIfScheduleIntervalIsGreaterThanZero() {

        viewModel = initViewModel(
            periodType = null,
            eventCreationType = EventCreationType.SCHEDULE,
            enrollmentStatus = EnrollmentStatus.ACTIVE,
            scheduleInterval = 20
        )

        composeTestRule.setContent {


            val date by viewModel.eventDate.collectAsState()
            val details by viewModel.eventDetails.collectAsState()
            ProvideInputDate(
                EventInputDateUiModel(
                    eventDate = date,
                    detailsEnabled = details.enabled,
                    onDateClick =  {} ,
                    onDateSelected = { dateValues ->
                        viewModel.onDateSet(dateValues.year, dateValues.month, dateValues.day)
                    },
                    onClear = { viewModel.onClearEventReportDate() },
                    required = true,
                )
            )

        }
        composeTestRule.onNodeWithTag(INPUT_EVENT_INITIAL_DATE).assertIsDisplayed()
        assert(viewModel.eventDate.value.dateValue == "9/9/2023")
    }

    @Test
    fun shouldNotAddStandardIntervalDaysIfScheduleIntervalIsZero() {

        viewModel = initViewModel(
            periodType = null,
            eventCreationType = EventCreationType.SCHEDULE,
            enrollmentStatus = EnrollmentStatus.ACTIVE,
            scheduleInterval = 0
        )

        composeTestRule.setContent {


            val date by viewModel.eventDate.collectAsState()
            val details by viewModel.eventDetails.collectAsState()
            ProvideInputDate(
                EventInputDateUiModel(
                    eventDate = date,
                    detailsEnabled = details.enabled,
                    onDateClick =  {},
                    onDateSelected = { dateValues ->
                        viewModel.onDateSet(dateValues.year, dateValues.month, dateValues.day)
                    },
                    onClear = { viewModel.onClearEventReportDate() },
                    required = true,
                )

            )

        }
        composeTestRule.onNodeWithTag(INPUT_EVENT_INITIAL_DATE).assertIsDisplayed()
        assert(viewModel.eventDate.value.dateValue == "20/8/2023")
    }

    @Test
    fun shouldShowEmptyCategorySelectorIfCategoryHasNoOptions() {

        viewModel = initViewModel(
            periodType = null,
            eventCreationType = EventCreationType.SCHEDULE,
            enrollmentStatus = EnrollmentStatus.ACTIVE,
            scheduleInterval = 0
        )
        composeTestRule.setContent {
            val date by viewModel.eventDate.collectAsState()
            val details by viewModel.eventDetails.collectAsState()
            val catCombo by viewModel.eventCatCombo.collectAsState()

            ProvideCategorySelector(
                modifier = Modifier.testTag(EMPTY_CATEGORY_SELECTOR),
                eventCatComboUiModel = EventCatComboUiModel(
                    EventCategory("UID", "NO OPTIONS ", 0, emptyList()),
                    eventCatCombo = catCombo,
                    detailsEnabled = details.enabled,
                    currentDate = date.currentDate,
                    selectedOrgUnit = details.selectedOrgUnit,
                    onClearCatCombo = {
                    },
                    onOptionSelected = {
                    },
                    required = true,
                    noOptionsText = "No options available",
                    catComboText = "No options catCombo",
                )
            )
        }
        composeTestRule.onNodeWithTag(EMPTY_CATEGORY_SELECTOR).assertIsDisplayed()
    }
}