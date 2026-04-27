package org.dhis2.usescases.searchTrackEntity

import dhis2.org.analytics.charts.Charts
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.forms.dataentry.SearchTEIRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.UiRenderType
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.extensions.getTodayAsInstant
import org.dhis2.mobile.commons.extensions.toKtxInstant
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.search.model.GeometryFeatureType
import org.dhis2.tracker.search.model.SyncState
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.dhis2.tracker.search.model.TrackedEntityTypeDomain
import org.dhis2.ui.ThemeManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.BooleanFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.event.internal.EventStatusFilterConnector
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemAttribute
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemHelper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.Date
import kotlin.time.Instant

class SearchRepositoryTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val dispatchers: DispatcherProvider =
        mock {
            on { io() } doReturn Dispatchers.IO
        }
    private lateinit var searchRepository: SearchRepositoryImplKt
    private lateinit var searchRepositoryJava: SearchRepository
    private val customIntentRepository: CustomIntentRepository = mock()

    private val trackedEntitySearchItemHelper: TrackedEntitySearchItemHelper = mock()

    private val enrollmentCollectionRepository: EnrollmentCollectionRepository = mock()
    private val stringFilterConnector: StringFilterConnector<EnrollmentCollectionRepository> =
        mock()
    private val booleanFilterConnector: BooleanFilterConnector<EnrollmentCollectionRepository> =
        mock()

    private val programCollectionRepository: ProgramCollectionRepository = mock()
    private val programReadOnlyOneObjectRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program> =
        mock()

    private val eventCollectionRepository: EventCollectionRepository = mock()
    private val eventStatusFilterConnector: EventStatusFilterConnector = mock()
    private val stringEventFilterConnector: StringFilterConnector<EventCollectionRepository> =
        mock()

    private val orgUnitCollectionRepository: OrganisationUnitCollectionRepository = mock()
    private val readOnlyOneObjectRepository: ReadOnlyOneObjectRepositoryFinalImpl<OrganisationUnit> =
        mock()

    private val filterPresenter: FilterPresenter = mock()
    private val resourceManager: ResourceManager = mock()
    private val charts: Charts = mock()
    private val crashReporterController: CrashReportController = mock()
    private val networkUtils: NetworkStatusProvider = mock()
    private val searchTEIRepository: SearchTEIRepository = mock()
    private val themeManager: ThemeManager = mock()
    private val profilePictureProvider: ProfilePictureProvider = mock()
    private val dateUtils: DateUtils = DateUtils()

    @Before
    fun setUp() {
        val trackedEntityAttributes =
            mapOf(
                "unique-code" to createTrackedEntityAttributeRepository("unique-code", true),
                "bp-number" to createTrackedEntityAttributeRepository("bp-number", true),
                "national-id" to createTrackedEntityAttributeRepository("national-id", true),
                "unique-id" to createTrackedEntityAttributeRepository("unique-id", true),
            )

        val trackedEntityAttributeCollection = mock<TrackedEntityAttributeCollectionRepository>()
        whenever(d2.trackedEntityModule().trackedEntityAttributes()).thenReturn(
            trackedEntityAttributeCollection,
        )
        whenever(trackedEntityAttributeCollection.uid(anyString())).thenAnswer { invocation ->
            val uid = invocation.arguments[0] as String
            trackedEntityAttributes[uid] ?: createTrackedEntityAttributeRepository(uid, false)
        }

        searchRepository =
            SearchRepositoryImplKt(
                searchRepositoryJava = mock(),
                d2 = d2,
                dispatcher = dispatchers,
                trackedEntityInstanceInfoProvider = mock(),
                eventInfoProvider = mock(),
                customIntentRepository = customIntentRepository,
            )

        searchRepositoryJava =
            SearchRepositoryImpl(
                "teiType",
                null,
                d2,
                filterPresenter,
                resourceManager,
                charts,
                crashReporterController,
                networkUtils,
                searchTEIRepository,
                themeManager,
                dateUtils,
                customIntentRepository,
                dispatchers
            )
    }

    @Test
    fun shouldTransformToSearchTeiModelWithOverdueEvents() {
        val overdueDate = dateUtils.getCalendarByDate(Date())
        overdueDate.add(Calendar.DATE, -2)

        val searchItemResult = createTrackedEntitySearchItemResult(
            uid = "teiUid",
            overDueDate = overdueDate.time.toKtxInstant(),
        )
        val sorting = SortingItem.create(Filters.ENROLLMENT_DATE)

        val result = searchRepository.mapTrackedEntitySearchItemResultToSearchTeiModel(
            searchItemResult,
            sorting,
        )

        assertNotNull(result.tei.overDueDate)
        assertTrue(result.tei.overDueDate!! < getTodayAsInstant())
    }

    @Test
    fun shouldTransformToSearchTeiModelWithOverdueScheduledEvents() {
        val overdueDate = dateUtils.getCalendarByDate(Date())
        overdueDate.add(Calendar.DATE, -2)

        val searchItemResult = createTrackedEntitySearchItemResult(
            uid = "teiUid",
            overDueDate = overdueDate.time.toKtxInstant(),
        )
        val sorting = SortingItem.create(Filters.ENROLLMENT_DATE)

        val result = searchRepository.mapTrackedEntitySearchItemResultToSearchTeiModel(
            searchItemResult,
            sorting,
        )

        assertNotNull(result.tei.overDueDate)
        assertTrue(result.tei.overDueDate!! < getTodayAsInstant())
    }

    @Test
    fun shouldTransformToSearchTeiModelWithOutOverdueEvents() {
        // Create a search result without overdue date (null means no overdue events)
        val searchItemResult = createTrackedEntitySearchItemResult(
            uid = "teiUid",
            overDueDate = null,
        )
        val sorting = SortingItem.create(Filters.ENROLLMENT_DATE)

        val result = searchRepository.mapTrackedEntitySearchItemResultToSearchTeiModel(
            searchItemResult,
            sorting,
        )

        // When there are no overdue events, overDueDate should be null
        assertNull(result.tei.overDueDate)
    }

    private fun mockedSdkCalls(
        searchItem: TrackedEntitySearchItem,
        teiToReturn: TrackedEntityInstance,
        enrollmentsInProgramToReturn: List<Enrollment> = listOf(),
        enrollmentsForInfoToReturn: List<Enrollment> = listOf(),
        eventsToReturn: List<Event> = listOf(),
        profilePathToReturn: String = "",
        orgUnitCount: Int = 1,
    ) {
        whenever(
            trackedEntitySearchItemHelper.toTrackedEntityInstance(searchItem),
        ) doReturn teiToReturn

        if (searchItem.isOnline) {
            whenever(d2.trackedEntityModule().trackedEntityInstances()) doReturn mock()
            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityInstances()
                    .uid(any()),
            ) doReturn mock()
            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityInstances()
                    .uid(any())
                    .blockingGet(),
            ) doReturn teiToReturn
        }

        whenever(d2.enrollmentModule().enrollments()) doReturn mock()
        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance(),
        ) doReturn mock()
        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq(any()),
        ) doReturn enrollmentCollectionRepository

        whenever(
            enrollmentCollectionRepository.byProgram(),
        ) doReturn stringFilterConnector
        whenever(
            stringFilterConnector.eq(any()),
        ) doReturn enrollmentCollectionRepository

        whenever(
            enrollmentCollectionRepository.byProgram().eq(any()),
        ) doReturn enrollmentCollectionRepository
        whenever(
            enrollmentCollectionRepository.orderByEnrollmentDate(RepositoryScope.OrderByDirection.DESC),
        ) doReturn enrollmentCollectionRepository
        whenever(
            enrollmentCollectionRepository.blockingGet(),
        ) doReturn enrollmentsInProgramToReturn

        // Mock setEnrollmentInfo
        whenever(
            enrollmentCollectionRepository.byDeleted(),
        ) doReturn booleanFilterConnector
        whenever(
            booleanFilterConnector.eq(any()),
        ) doReturn enrollmentCollectionRepository
        whenever(
            enrollmentCollectionRepository.byDeleted().eq(false),
        ) doReturn enrollmentCollectionRepository
        whenever(
            enrollmentCollectionRepository.orderByCreated(RepositoryScope.OrderByDirection.DESC),
        ) doReturn enrollmentCollectionRepository
        whenever(
            enrollmentCollectionRepository.blockingGet(),
        ) doReturn enrollmentsForInfoToReturn

        val programUid =
            if (enrollmentsForInfoToReturn.isNotEmpty()) enrollmentsForInfoToReturn[0].program()!! else "programUid"
        whenever(d2.programModule().programs()) doReturn programCollectionRepository
        whenever(
            programCollectionRepository.uid(any()),
        ) doReturn programReadOnlyOneObjectRepository
        whenever(
            programReadOnlyOneObjectRepository.blockingGet(),
        ) doReturn
            Program
                .builder()
                .uid(programUid)
                .displayFrontPageList(true)
                .categoryCombo(ObjectWithUid.create("categoryComboUid"))
                .enrollmentCategoryCombo(ObjectWithUid.create("categoryComboUid"))
                .build()

        // Mock setOverdueEvents
        whenever(d2.eventModule().events()) doReturn eventCollectionRepository
        whenever(
            eventCollectionRepository.byEnrollmentUid(),
        ) doReturn stringEventFilterConnector
        whenever(
            stringEventFilterConnector.`in`(any<Collection<String>>()),
        ) doReturn eventCollectionRepository
        whenever(
            eventCollectionRepository.byEnrollmentUid().`in`(any<Collection<String>>()),
        ) doReturn eventCollectionRepository
        whenever(
            eventCollectionRepository.byStatus(),
        ) doReturn eventStatusFilterConnector
        whenever(
            eventStatusFilterConnector.eq(EventStatus.OVERDUE),
        ) doReturn eventCollectionRepository
        whenever(
            eventCollectionRepository.byStatus().eq(EventStatus.OVERDUE),
        ) doReturn eventCollectionRepository
        whenever(
            eventCollectionRepository.byProgramUid(),
        ) doReturn stringEventFilterConnector
        whenever(
            eventCollectionRepository.byProgramUid().eq(any()),
        ) doReturn eventCollectionRepository
        whenever(
            eventCollectionRepository.orderByDueDate(RepositoryScope.OrderByDirection.DESC),
        ) doReturn eventCollectionRepository
        whenever(
            eventCollectionRepository.blockingGet(),
        ) doReturn eventsToReturn.filter { it.status() == EventStatus.OVERDUE }
        whenever(eventStatusFilterConnector.eq(EventStatus.SCHEDULE)).thenReturn(
            eventCollectionRepository,
        )
        whenever(eventCollectionRepository.byStatus().eq(EventStatus.SCHEDULE)).thenReturn(
            eventCollectionRepository,
        )
        whenever(eventCollectionRepository.byProgramUid().eq(any())).thenReturn(
            eventCollectionRepository,
        )
        whenever(eventCollectionRepository.orderByDueDate(RepositoryScope.OrderByDirection.DESC)).thenReturn(
            eventCollectionRepository,
        )
        whenever(eventCollectionRepository.blockingGet()).thenReturn(eventsToReturn.filter { it.status() == EventStatus.SCHEDULE })
        // mock orgUnitName(orgUnitUid)
        whenever(
            d2.organisationUnitModule().organisationUnits(),
        ) doReturn orgUnitCollectionRepository
        whenever(
            orgUnitCollectionRepository.uid(any()),
        ) doReturn readOnlyOneObjectRepository
        whenever(readOnlyOneObjectRepository.blockingGet()) doReturn
            OrganisationUnit
                .builder()
                .uid("uid")
                .displayName("orgUnitName")
                .build()

        whenever(profilePictureProvider.invoke(any(), any())) doReturn profilePathToReturn

        // mock displayOrgUnit()
        whenever(
            orgUnitCollectionRepository.byProgramUids(any()),
        ) doReturn orgUnitCollectionRepository
        whenever(
            orgUnitCollectionRepository.blockingCount(),
        ) doReturn orgUnitCount
    }

    private fun getTrackedEntitySearchItem(
        header: String?,
        isOnline: Boolean = false,
        state: State = State.SYNCED,
        attributesValues: List<TrackedEntitySearchItemAttribute> = listOf(),
    ): TrackedEntitySearchItem =
        TrackedEntitySearchItem(
            uid = "uid",
            created = Date(),
            lastUpdated = Date(),
            createdAtClient = Date(),
            lastUpdatedAtClient = Date(),
            organisationUnit = "orgUnit",
            geometry = null,
            syncState = state,
            aggregatedSyncState = state,
            deleted = false,
            isOnline = isOnline,
            type = TrackedEntityType.builder().uid("uid").build(),
            header = header,
            attributeValues = attributesValues,
        )

    private fun createEnrollment(
        uid: String,
        orgUnitUid: String,
        programUid: String,
        status: EnrollmentStatus = EnrollmentStatus.ACTIVE,
    ) = Enrollment
        .builder()
        .uid(uid)
        .organisationUnit(orgUnitUid)
        .program(programUid)
        .status(status)
        .attributeOptionCombo("attributeOptionComboUid")
        .build()

    private fun createEvent(
        uid: String,
        status: EventStatus = EventStatus.ACTIVE,
        dueDate: Date = Date(),
    ) = Event
        .builder()
        .uid(uid)
        .status(status)
        .dueDate(dueDate)
        .build()

    private fun createTrackedEntityAttributeRepository(
        uid: String,
        unique: Boolean,
    ): ReadOnlyOneObjectRepositoryFinalImpl<TrackedEntityAttribute> {
        val attribute =
            mock<TrackedEntityAttribute> {
                on { uid() } doReturn uid
                on { unique() } doReturn unique
            }
        return mock {
            on { blockingGet() } doReturn attribute
        }
    }

    private fun createTrackedEntitySearchItemResult(
        uid: String,
        overDueDate: Instant?,
    ): TrackedEntitySearchItemResult {
        return TrackedEntitySearchItemResult(
            uid = uid,
            created = null,
            lastUpdated = null,
            createdAtClient = null,
            lastUpdatedAtClient = null,
            ownerOrgUnit = null,
            enrollmentOrgUnit = null,
            shouldDisplayOrgUnit = false,
            geometry = null,
            syncState = SyncState.SYNCED,
            aggregatedSyncState = SyncState.SYNCED,
            deleted = false,
            isOnline = false,
            teTypeName = "Person",
            type = TrackedEntityTypeDomain(
                trackedEntityTypeAttributeDomains = emptyList(),
                featureType = GeometryFeatureType.NONE,
            ),
            header = "Test Header",
            overDueDate = overDueDate,
            selectedEnrollment = null,
            profilePicture = null,
            enrolledPrograms = null,
            enrollments = null,
            relationships = null,
            defaultTypeIcon = null,
            attributeValues = emptyList(),
        )
    }

    private fun createMockData(): List<FieldUiModel> =
        listOf(
            FieldUiModelImpl(
                uid = "first-name",
                label = "First Name",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "unique-code",
                label = "Unique Code",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.QR_CODE,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "last-name",
                label = "Last Name",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "phone-number",
                label = "Phone Number",
                valueType = ValueType.PHONE_NUMBER,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "unique-id",
                label = "Unique ID",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "state",
                label = "State",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "national-id",
                label = "National ID",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "qr-code",
                label = "qr-code",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.QR_CODE,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "bar-code",
                label = "bar-code",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.BAR_CODE,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "bp-number",
                label = "BP Number",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.BAR_CODE,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
        )
}
