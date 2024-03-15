package org.dhis2.usescases.programEventDetail.eventList.mapper

import android.content.Context
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class EventCardMapperTest {

    private val context: Context = mock()
    private val resourceManager: ResourceManager = mock()
    private val currentDate = Date()

    private lateinit var mapper: EventCardMapper

    @Before
    fun setUp() {
        whenever(context.getString(R.string.interval_now)) doReturn "now"
        whenever(context.getString(R.string.filter_period_today)) doReturn "Today"
        whenever(resourceManager.getString(R.string.show_more)) doReturn "Show more"
        whenever(resourceManager.getString(R.string.show_less)) doReturn "Show less"
        whenever(resourceManager.getString(R.string.completed)) doReturn "Completed"
        whenever(
            resourceManager.getString(R.string.event_completed),
        ) doReturn "Event Completed"

        mapper = EventCardMapper(context, resourceManager)
    }

    @Test
    fun shouldReturnCardFull() {
        val model = createFakeModel()

        val result = mapper.map(
            event = model,
            editable = true,
            displayOrgUnit = true,
            onSyncIconClick = {},
            onCardClick = {},
        )

        assertEquals(result.title, model.displayDate)
        assertEquals(result.lastUpdated, model.lastUpdate.toDateSpan(context))
        assertEquals(result.additionalInfo[0].value, model.dataElementValues?.get(0)?.second)
        assertEquals(result.additionalInfo[1].value, model.orgUnitName)
        assertEquals(result.additionalInfo[2].value, model.catComboName)
        assertEquals(
            result.additionalInfo[3].value,
            resourceManager.getString(R.string.event_completed),
        )
    }

    private fun createFakeModel(): EventViewModel {
        val dataElements = mutableListOf<Pair<String, String>>()
        dataElements.add(
            Pair("Name", "Peter"),
        )

        return EventViewModel(
            type = EventViewModelType.EVENT,
            stage = null,
            event = Event.builder()
                .uid("EventUi")
                .status(EventStatus.COMPLETED)
                .dueDate(currentDate)
                .aggregatedSyncState(State.SYNCED)
                .build(),
            eventCount = 0,
            lastUpdate = currentDate,
            isSelected = false,
            canAddNewEvent = false,
            orgUnitName = "Org unit name",
            catComboName = "Cat combo name",
            dataElementValues = dataElements,
            groupedByStage = null,
            valueListIsOpen = false,
            showTopShadow = false,
            showBottomShadow = false,
            displayDate = "21/11/2023",
            nameCategoryOptionCombo = "Name Category option combo",
            metadataIconData = MetadataIconData.defaultIcon(),
        )
    }
}
