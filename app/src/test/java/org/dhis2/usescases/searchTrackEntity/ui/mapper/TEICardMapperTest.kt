package org.dhis2.usescases.searchTrackEntity.ui.mapper

import android.content.Context
import org.dhis2.R
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.date.toOverdueOrScheduledUiText
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.extensions.toJavaDate
import org.dhis2.mobile.commons.extensions.toKtxInstant
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.search.model.DomainEnrollment
import org.dhis2.tracker.search.model.DomainObjectStyle
import org.dhis2.tracker.search.model.DomainProgram
import org.dhis2.tracker.search.model.EnrollmentStatus
import org.dhis2.tracker.search.model.GeometryFeatureType
import org.dhis2.tracker.search.model.SyncState
import org.dhis2.tracker.search.model.TrackedEntitySearchItemAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.dhis2.tracker.search.model.TrackedEntityTypeAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntityTypeDomain
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.Date
import kotlin.time.Instant

class TEICardMapperTest {
    private val context: Context = mock()
    private val resourceManager: ResourceManager = mock()

    private lateinit var mapper: TEICardMapper

    private val enrollmentUid = "EnrollmentUid"
    private val programUid = "programUid"
    private val enrollmentOrgUnit = "OrgUnit"
    private val teiUid = "TEIUid"
    private val selectedEnrollment =      DomainEnrollment(
        uid = enrollmentUid,
        orgUnit = enrollmentOrgUnit,
        program = programUid,
        enrollmentDate = Instant.parse("2020-01-01T00:00:00.00Z"),
        incidentDate = Instant.parse("2020-01-01T00:00:00.00Z"),
        completedDate = Instant.parse("2020-01-01T00:00:00.00Z"),
        followUp = true,
        status = EnrollmentStatus.COMPLETED,
        trackedEntityInstance = teiUid,
    )
    private val enrollments = listOf(
        selectedEnrollment
    )
    @Before
    fun setUp() {
        whenever(context.getString(R.string.interval_now)) doReturn "now"
        whenever(context.getString(R.string.filter_period_today)) doReturn "Today"
        whenever(resourceManager.getString(R.string.show_more)) doReturn "Show more"
        whenever(resourceManager.getString(R.string.show_less)) doReturn "Show less"
        whenever(resourceManager.getString(R.string.completed)) doReturn "Completed"
        whenever(
            resourceManager.formatWithEnrollmentLabel(any(), any(), any(), any()),
        ) doReturn "Enrollment Completed"
        whenever(
            resourceManager.getString(R.string.overdue_today),
        ) doReturn "Today"
        whenever(resourceManager.getString(R.string.marked_follow_up)) doReturn "Marked for follow-up"

        mapper = TEICardMapper(context, resourceManager)
    }

    @Test
    fun shouldReturnCardFull() {
        val model = createFakeModel()

        val result =
            mapper.map(
                searchTEIModel = model,
                onSyncIconClick = {},
                onCardClick = {},
                onImageClick = {},
            )

        assertEquals(result.title, model.header)
        assertEquals(result.lastUpdated, model.tei.lastUpdated?.toJavaDate().toDateSpan(context))
        assertEquals(result.additionalInfo[0].value, model.attributeValues["Name"]?.value)
        assertEquals(result.additionalInfo[1].value, model.tei.enrollmentOrgUnit)
        assertEquals(
            result.additionalInfo[2].value,
            model.tei.enrolledPrograms?.joinToString(", ") { it.displayName },
        )
        assertEquals(
            result.additionalInfo[3].value,
            "Enrollment Completed",
        )

        assertEquals(
            result.additionalInfo[4].value,
            model.tei.overDueDate?.toJavaDate().toOverdueOrScheduledUiText(resourceManager),
        )
        assertEquals(
            result.additionalInfo[5].value,
            resourceManager.getString(R.string.marked_follow_up),
        )
    }

    @Test
    fun shouldShowOverDueLabel() {
        val overdueDate = DateUtils.getInstance().calendar
        overdueDate.add(Calendar.DATE, -2)

        whenever(resourceManager.getPlural(any(), any(), any())) doReturn "2 days"

        val model = createFakeModel(overdueDate.time)

        val result =
            mapper.map(
                searchTEIModel = model,
                onSyncIconClick = {},
                onCardClick = {},
                onImageClick = {},
            )
        assertEquals(
            result.additionalInfo[4].value,
            model.tei.overDueDate?.toJavaDate().toOverdueOrScheduledUiText(resourceManager),
        )
    }

    private fun createFakeModel(
        currentDate: Date = Date(),
    ): SearchTeiModel {
        val attributeValues = LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain>()
        val attribute =   TrackedEntitySearchItemAttributeDomain(
            attribute = "attrUid1",
            displayName = "Name",
            displayFormName = "Name",
            value = "Peter",
            valueType = TrackerInputType.TEXT,
            displayInList = true,
            optionSet = null
        )
        attributeValues["Name"] = attribute

        val tei = TrackedEntitySearchItemResult(
            uid = "teiUid",
            created = Instant.parse("2020-01-01T00:00:00.00Z"),
            lastUpdated = Instant.parse("2020-01-01T00:00:00.00Z"),
            createdAtClient = Instant.parse("2020-01-01T00:00:00.00Z"),
            lastUpdatedAtClient = Instant.parse("2020-01-01T00:00:00.00Z"),
            ownerOrgUnit = "ownerOrgUnit",
            enrollmentOrgUnit = "enrollmentOrgUnit",
            shouldDisplayOrgUnit = true,
            geometry = null,
            syncState = SyncState.SYNCED,
            aggregatedSyncState = SyncState.SYNCED,
            deleted = false,
            isOnline = true,
            teTypeName = "teTypeName",
            type = TrackedEntityTypeDomain(
                trackedEntityTypeAttributeDomains = listOf(TrackedEntityTypeAttributeDomain(
                    trackedEntityTypeUid=  "trackedEntityTypeUid",
                    trackedEntityAttributeUid = "trackedEntityAttributeUid",
                    displayInList= true,
                    mandatory = false,
                    searchable = true,
                    sortOrder = 1,
                )),
                featureType = GeometryFeatureType.POINT,
            ),
            header = "TEI header",
            overDueDate = currentDate.toKtxInstant(),
            selectedEnrollment = selectedEnrollment,
            profilePicture = null,
            enrolledPrograms = listOf(
                DomainProgram(
                uid = "Program1Uid",
                displayName = "Program 1",
                style = DomainObjectStyle(
                    icon = "iconUid",
                    color = "colorUid"
                )
            ),
                DomainProgram(
                uid = "Program2Uid",
                displayName = "Program 2",
                style = DomainObjectStyle(
                    icon = "iconUid2",
                    color = "colorUid2"
                )
            ),),
            enrollments = enrollments,
            relationships = null,
            defaultTypeIcon = null,
            attributeValues = listOf(attribute),
        )
        val searchTeiModel = SearchTeiModel()
        searchTeiModel.tei = tei
        searchTeiModel.attributeValues = attributeValues

        return searchTeiModel
    }
}
