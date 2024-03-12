package org.dhis2.usescases.eventsWithoutRegistration.eventDetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.MetadataIconProvider
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
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui.EventDetailsViewModel
import org.dhis2.utils.MainCoroutineScopeRule
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
class EventDetailsIntegrationTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Needs context
    private val locationProvider: LocationProvider = mock()
    private val resourceManager: ResourceManager = mock()
    private val periodUtils: DhisPeriodUtils = mock()
    private val preferencesProvider: PreferenceProvider = mock()

    // Preconditions, data source
    private val style: ObjectStyle = mock()
    private val eventDate = Date()
    private val event: Event = mock {
        on { organisationUnit() } doReturn ORG_UNIT_UID
        on { eventDate() } doReturn eventDate
    }
    private val programStage: ProgramStage = mock {
        on { displayName() } doReturn PROGRAM_STAGE_NAME
        on { executionDateLabel() } doReturn EXECUTION_DATE
        on { uid() } doReturn PROGRAM_STAGE
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
    private val eventDetailsRepository: EventDetailsRepository = mock {
        on { getProgramStage() } doReturn programStage
        on { catCombo() } doReturn catCombo
        on { getEvent() } doReturn event
        on { getObjectStyle() } doReturn style
        on { getOrganisationUnit(ORG_UNIT_UID) } doReturn orgUnit
        on { getGeometryModel() } doReturn geometryModel
        on { getCatOptionCombos(CAT_COMBO_UID) } doReturn listOf(categoryOptionCombo)
        on { getEditableStatus() } doReturn EventEditableStatus.Editable()
    }

    private val metadataIconProvider: MetadataIconProvider = mock()

    private lateinit var viewModel: EventDetailsViewModel

    @Test
    fun `should reopen a completed event`() = runBlocking {
        // Given an event that can be reopened
        whenever(eventDetailsRepository.getCanReopen()) doReturn true

        // AND is completed
        viewModel = initViewModel(
            periodType = null,
            enrollmentStatus = EnrollmentStatus.COMPLETED,
        )

        // When user taps on reopen
        whenever(eventDetailsRepository.reopenEvent()) doReturn Result.success(Unit)
        whenever(eventDetailsRepository.getCanReopen()) doReturn false
        viewModel.onReopenClick()

        // Then event should be opened
        assertFalse(viewModel.eventDetails.value.canReopen)
    }

    private fun initViewModel(
        periodType: PeriodType?,
        eventCreationType: EventCreationType = EventCreationType.DEFAULT,
        enrollmentStatus: EnrollmentStatus,
    ) = EventDetailsViewModel(
        configureEventDetails = createConfigureEventDetails(
            eventCreationType,
            enrollmentStatus,
        ),
        configureEventReportDate = createConfigureEventReportDate(
            eventCreationType,
            periodType,
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
    ) = ConfigureEventReportDate(
        creationType = eventCreationType,
        resourceProvider = provideEventResourcesProvider(),
        repository = eventDetailsRepository,
        periodType = periodType,
        periodUtils = periodUtils,
        enrollmentId = ENROLLMENT_UID,
        scheduleInterval = 0,
    )

    private fun createConfigureEventDetails(
        eventCreationType: EventCreationType,
        enrollmentStatus: EnrollmentStatus,
    ) = ConfigureEventDetails(
        repository = eventDetailsRepository,
        resourcesProvider = provideEventResourcesProvider(),
        creationType = eventCreationType,
        enrollmentStatus = enrollmentStatus,
        metadataIconProvider = metadataIconProvider,
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
        const val PROGRAM_STAGE = "programStage"
    }
}
