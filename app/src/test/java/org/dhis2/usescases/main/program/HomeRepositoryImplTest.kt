package org.dhis2.usescases.main.program

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.time.Instant
import java.util.Date
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummary
import org.hisp.dhis.android.core.period.DatePeriod
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

class HomeRepositoryImplTest {

    private lateinit var homeRepository: HomeRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val scheduler = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        homeRepository = HomeRepositoryImpl(
            d2,
            "event",
            "dataSet",
            "tei",
            scheduler
        )
        whenever(
            d2.dataSetModule().dataSets().uid(anyString()).blockingGet()
        ) doReturn DataSet.builder()
            .uid("dataSetUid")
            .description("description")
            .style(
                ObjectStyle.builder()
                    .color("color")
                    .icon("icon")
                    .build()
            )
            .access(
                Access.create(
                    true,
                    true,
                    DataAccess.create(
                        true,
                        true
                    )
                )
            )
            .build()
    }

    @Test
    fun `Should return list of data set ProgramViewModel`() {
        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .get()
        ) doReturn Single.just(mockedDataSetInstanceSummaries())

        val testObserver = homeRepository.aggregatesModels(
            emptyList(),
            emptyList(),
            emptyList(),
            false
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 2
            }
    }

    @Test
    fun `Should filter list of data set ProgramViewModel by dates`() {
        val testingDatePeriods = listOf(
            DatePeriod.create(
                Date.from(Instant.parse("2010-12-01T00:00:00.00Z")),
                Date.from(Instant.parse("2010-12-02T00:00:00.00Z"))
            )
        )
        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .byPeriodStartDate().inDatePeriods(testingDatePeriods)
                .get()
        ) doReturn Single.just(mockedDataSetInstanceSummaries())

        val testObserver = homeRepository.aggregatesModels(
            testingDatePeriods,
            emptyList(),
            emptyList(),
            false
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 2
            }
    }

    @Test
    fun `Should filter list of data set ProgramViewModel by orgUnit`() {
        val orgUnitFilter = listOf("orgUnit")
        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .byOrganisationUnitUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .byOrganisationUnitUid().`in`(orgUnitFilter)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .byOrganisationUnitUid().`in`(orgUnitFilter)
                .get()
        ) doReturn Single.just(mockedDataSetInstanceSummaries())

        val testObserver = homeRepository.aggregatesModels(
            emptyList(),
            orgUnitFilter,
            emptyList(),
            false
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 2
            }
    }

    @Test
    fun `Should filter list of data set ProgramViewModel by state`() {
        val stateFilter = listOf(State.TO_UPDATE)

        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .byState()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .byState().`in`(stateFilter)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .byState().`in`(stateFilter)
                .get()
        ) doReturn Single.just(mockedDataSetInstanceSummaries())

        val testObserver = homeRepository.aggregatesModels(
            emptyList(),
            emptyList(),
            stateFilter,
            false
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 2
            }
    }

    @Test
    fun `Should filter list of data set ProgramViewModel by assigned`() {
        val assignedFilter = true
        whenever(
            d2.dataSetModule().dataSetInstanceSummaries()
                .get()
        ) doReturn Single.just(mockedDataSetInstanceSummaries())

        val testObserver = homeRepository.aggregatesModels(
            emptyList(),
            emptyList(),
            emptyList(),
            assignedFilter
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 2 &&
                    it[0].count() == 0 &&
                    it[0].translucent() &&
                    it[1].count() == 0 &&
                    it[1].translucent()
            }
    }

    private fun mockedDataSetInstanceSummaries(): List<DataSetInstanceSummary> {
        return listOf(
            DataSetInstanceSummary.builder()
                .dataSetUid("dataSetUid_1")
                .dataSetDisplayName("dataSetUid_1")
                .valueCount(5)
                .dataSetInstanceCount(2)
                .state(State.SYNCED)
                .build(),
            DataSetInstanceSummary.builder()
                .dataSetUid("dataSetUid_1")
                .dataSetDisplayName("dataSetUid_1")
                .dataSetInstanceCount(1)
                .valueCount(5)
                .state(State.TO_UPDATE)
                .build()
        )
    }
}
