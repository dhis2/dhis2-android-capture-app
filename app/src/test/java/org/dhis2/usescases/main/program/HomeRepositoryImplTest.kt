package org.dhis2.usescases.main.program

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.time.Instant
import java.util.Date

class HomeRepositoryImplTest {

    private lateinit var homeRepository: HomeRepository
    private lateinit var mockedDataSetList: List<DataSet>
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

        mockedDataSetList = mockedDataSetList()
        whenever(d2.dataSetModule().dataSets().blockingGet()) doReturn mockedDataSetList
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
        ) doReturn mock()

        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(anyString())
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(anyString())
                .blockingGet()
        ) doReturn emptyList()
    }

    @Test
    fun `Should return list of data set ProgramViewModel`() {

        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .blockingGet()
        ) doReturn mockedDataSetInstanceList()

        val testObserver = homeRepository.aggregatesModels(
            emptyList(),
            emptyList(),
            emptyList(),
            false
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 10
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
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .byPeriodStartDate()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .byPeriodStartDate().inDatePeriods(testingDatePeriods)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .blockingGet()
        ) doReturn emptyList()

        val testObserver = homeRepository.aggregatesModels(
            testingDatePeriods,
            emptyList(),
            emptyList(),
            false
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 10
            }
        verify(
            d2.dataSetModule().dataSetInstances().byDataSetUid().eq(anyString()),
            times(1 + mockedDataSetList.size)
        )
            .byPeriodStartDate()
    }

    @Test
    fun `Should filter list of data set ProgramViewModel by orgUnit`() {
        val orgUnitFilter = listOf("orgUnit")
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .byOrganisationUnitUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .byOrganisationUnitUid().`in`(orgUnitFilter)
        ) doReturn mock()
        whenever(d2.dataSetModule().dataSetInstances().byDataSetUid().eq(anyString()).blockingGet()) doReturn emptyList()

        val testObserver = homeRepository.aggregatesModels(
            emptyList(),
            orgUnitFilter,
            emptyList(),
            false
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 10
            }
        verify(
            d2.dataSetModule().dataSetInstances().byDataSetUid().eq(anyString()),
            times(1 + mockedDataSetList.size)
        )
            .byOrganisationUnitUid()
    }

    @Test
    fun `Should filter list of data set ProgramViewModel by state`() {
        val stateFilter = listOf(State.TO_UPDATE)

        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .byState()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .byState().`in`(stateFilter)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .blockingGet()
        ) doReturn mockedDataSetInstanceList()


        val testObserver = homeRepository.aggregatesModels(
            emptyList(),
            emptyList(),
            stateFilter,
            false
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it[0].count() == 1
            }
    }

    @Test
    fun `Should filter list of data set ProgramViewModel by assigned`() {
        val assignedFilter = true
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(anyString())
                .blockingGet()
        ) doReturn mockedDataSetInstanceList()

        val testObserver = homeRepository.aggregatesModels(
            emptyList(),
            emptyList(),
            emptyList(),
            assignedFilter
        ).test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it.size == 10
            }

        verify(
            d2.dataSetModule().dataSetInstances().byDataSetUid().eq(anyString()),
            times(0)
        )
            .blockingCount()
    }

    private fun mockedDataSetList(): List<DataSet> {
        val list = mutableListOf<DataSet>()
        for (i in 1..10) {
            list.add(
                DataSet.builder()
                    .uid("dataSetUid_${i}")
                    .displayName("dataSetName_${i}")
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
            )
        }
        return list
    }

    private fun mockedDataSetInstanceList(): List<DataSetInstance> {
        return listOf(
            DataSetInstance.builder()
                .dataSetUid("dataSetUid_1")
                .dataSetDisplayName("dataSetUid_1")
                .period("periodId")
                .periodType(PeriodType.Daily)
                .organisationUnitUid("orgUnitUid")
                .organisationUnitDisplayName("orgUnitName")
                .attributeOptionComboUid("optionComboUid")
                .attributeOptionComboDisplayName("optionComboName")
                .valueCount(5)
                .completed(false)
                .completionDate(null)
                .dataValueState(State.SYNCED)
                .completionState(State.SYNCED)
                .state(State.SYNCED)
                .build(),
            DataSetInstance.builder()
                .dataSetUid("dataSetUid_1")
                .dataSetDisplayName("dataSetUid_1")
                .period("periodId")
                .periodType(PeriodType.Daily)
                .organisationUnitUid("orgUnitUid")
                .organisationUnitDisplayName("orgUnitName")
                .attributeOptionComboUid("optionComboUid")
                .attributeOptionComboDisplayName("optionComboName")
                .valueCount(5)
                .completed(false)
                .completionDate(null)
                .dataValueState(State.TO_UPDATE)
                .completionState(State.TO_UPDATE)
                .state(State.TO_UPDATE)
                .build()
        )
    }

}