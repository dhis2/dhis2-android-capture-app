package org.dhis2.usescases.events

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import org.dhis2.data.dhislogic.DhisEventUtils
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ScheduledEventPresenterImplTest {

    lateinit var presenter: ScheduledEventPresenterImpl
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val view: ScheduledEventContract.View = mock()
    private val eventUtils: DhisEventUtils = mock()

    @Before
    fun setUp() {
        presenter = ScheduledEventPresenterImpl(view, d2, "eventUid", eventUtils)
    }

    @Test
    fun `Should open event initial screen if the event needs extra info`() {
        whenever(
            d2.eventModule().events().uid(any())
        ) doReturn mock()
        whenever(
            eventUtils.newEventNeedsExtraInfo(any())
        ) doReturn true

        presenter.setEventDate(Date())

        verify(view).openInitialActivity()
    }

    @Test
    fun `Should open event form screen if the event does not need coordinates or catCombo`() {
        whenever(
            d2.eventModule().events().uid(any())
        ) doReturn mock()
        whenever(
            eventUtils.newEventNeedsExtraInfo(any())
        ) doReturn false

        presenter.setEventDate(Date())

        verify(view).openFormActivity()
    }
}
