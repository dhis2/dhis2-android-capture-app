package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.hisp.dhis.android.core.arch.helpers.DateUtils
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.spy
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SchedulingViewModelTest {

    private lateinit var schedulingViewModel: SchedulingViewModel

    private val testingDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        schedulingViewModel = SchedulingViewModel(
            d2 = mock(),
            resourceManager = mock(),
            periodUtils = mock(),
        )
    }

    @Test
    fun shouldSetReportDate() {
        val date = DateUtils.DATE_FORMAT.parse("2024-04-14T00:00:00.000")
        val spy = spy(schedulingViewModel)
        spy.onDateSet(2024, 4, 14)
        verify(spy).setUpEventReportDate(date)
    }

    @Test
    fun shouldSetReportDateForIncreasedNumberOfMonth() {
        val date = DateUtils.DATE_FORMAT.parse("2025-01-31T00:00:00.000")
        val spy = spy(schedulingViewModel)
        spy.onDateSet(2024, 13, 31)
        verify(spy).setUpEventReportDate(date)
    }
}
