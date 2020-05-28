package org.dhis2.usescases.datasets.dataSetTable.dataSetTable.dataSetDetail

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepository
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailPresenter
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailView
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Before
import org.junit.Test

class DataSetDetailPresenterTest {

    private val view: DataSetDetailView = mock()
    private val repository: DataSetTableRepository = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private lateinit var presenter: DataSetDetailPresenter

    @Before
    fun setUp() {
        presenter = DataSetDetailPresenter(
            view,
            repository,
            scheduler
        )
    }

    @Test
    fun `Should init with call to setCatOptComboName`() {
        whenever(repository.dataSetCatComboName) doReturn Single.just("catOptionName")
        whenever(repository.dataSetInstance()) doReturn Flowable.just(defaultDataSetInstance)
        whenever(repository.period) doReturn Flowable.just(defaultPeriod)
        whenever(repository.dataSet) doReturn Flowable.just(defaultDataSet)

        presenter.init()

        verify(view).setCatOptComboName("catOptionName")
        verify(view).setDataSetDetails(defaultDataSetInstance, defaultPeriod)
        verify(view).setStyle(defaultStyle)
    }

    @Test
    fun `Should init with call to hideCatOptCombo`() {
        whenever(repository.dataSetCatComboName) doReturn Single.just("")
        whenever(repository.dataSetInstance()) doReturn Flowable.just(defaultDataSetInstance)
        whenever(repository.period) doReturn Flowable.just(defaultPeriod)
        whenever(repository.dataSet) doReturn Flowable.just(defaultDataSet)

        presenter.init()

        verify(view).hideCatOptCombo()
        verify(view).setDataSetDetails(defaultDataSetInstance, defaultPeriod)
        verify(view).setStyle(defaultStyle)
    }

    private val defaultDataSetInstance: DataSetInstance
        get() = DataSetInstance.builder()
            .attributeOptionComboUid("aocuid")
            .attributeOptionComboDisplayName("aocname")
            .completed(false)
            .dataSetDisplayName("name")
            .dataSetUid("uid")
            .organisationUnitDisplayName("ouname")
            .organisationUnitUid("ouuid")
            .period("period")
            .periodType(PeriodType.Daily)
            .valueCount(0)
            .build()

    private val defaultPeriod: Period
        get() = Period.builder()
            .periodId("period")
            .build()

    private val defaultDataSet: DataSet
        get() = DataSet.builder()
            .uid("datasetuid")
            .style(defaultStyle)
            .build()

    private val defaultStyle: ObjectStyle
        get() = ObjectStyle.builder()
            .icon("icom")
            .color("color")
            .build()
}
