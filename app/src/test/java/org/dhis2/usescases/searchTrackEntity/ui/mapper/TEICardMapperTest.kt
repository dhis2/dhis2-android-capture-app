package org.dhis2.usescases.searchTrackEntity.ui.mapper

import android.content.Context
import org.dhis2.R
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.date.toOverdueOrScheduledUiText
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class TEICardMapperTest {

    private val context: Context = mock()
    private val resourceManager: ResourceManager = mock()
    private val currentDate = Date()

    private lateinit var mapper: TEICardMapper

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

        val result = mapper.map(
            searchTEIModel = model,
            onSyncIconClick = {},
            onCardClick = {},
            onImageClick = {},
        )

        assertEquals(result.title, model.header)
        assertEquals(result.lastUpdated, model.tei.lastUpdated().toDateSpan(context))
        assertEquals(result.additionalInfo[0].value, model.attributeValues["Name"]?.value())
        assertEquals(result.additionalInfo[1].value, model.enrolledOrgUnit)
        assertEquals(
            result.additionalInfo[2].value,
            model.programInfo.map { it.name() }.joinToString(", "),
        )
        assertEquals(
            result.additionalInfo[3].value,
            "Enrollment Completed",
        )

        assertEquals(
            result.additionalInfo[4].value,
            model.overdueDate.toOverdueOrScheduledUiText(resourceManager),
        )
        assertEquals(
            result.additionalInfo[5].value,
            resourceManager.getString(R.string.marked_follow_up),
        )
    }

    private fun createFakeModel(): SearchTeiModel {
        val attributeValues = LinkedHashMap<String, TrackedEntityAttributeValue>()
        attributeValues["Name"] = TrackedEntityAttributeValue.builder()
            .value("Peter")
            .build()

        val model = SearchTeiModel().apply {
            header = "TEI header"
            tei = TrackedEntityInstance.builder()
                .uid("TEIUid")
                .lastUpdated(currentDate)
                .organisationUnit("OrgUnit")
                .aggregatedSyncState(State.SYNCED)
                .build()
            enrolledOrgUnit = "OrgUnit"
            displayOrgUnit = true
            setCurrentEnrollment(
                Enrollment.builder()
                    .uid("EnrollmentUid")
                    .program("programUid")
                    .status(EnrollmentStatus.COMPLETED)
                    .build(),
            )
            setAttributeValues(attributeValues)

            addProgramInfo(
                Program.builder()
                    .uid("Program1Uid")
                    .displayName("Program 1")
                    .build(),
                null,
            )
            addProgramInfo(
                Program.builder()
                    .uid("Program2Uid")
                    .displayName("Program 2")
                    .build(),
                null,
            )
            overdueDate = currentDate
            isHasOverdue = true

            addEnrollment(
                Enrollment.builder()
                    .uid("EnrollmentUid")
                    .followUp(true)
                    .build(),
            )
        }
        return model
    }
}
