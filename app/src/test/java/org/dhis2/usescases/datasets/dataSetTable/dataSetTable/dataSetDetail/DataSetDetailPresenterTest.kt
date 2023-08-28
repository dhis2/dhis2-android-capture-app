package org.dhis2.usescases.datasets.dataSetTable.dataSetTable.dataSetDetail

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepositoryImpl
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailPresenter
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailView
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DataSetDetailPresenterTest {

    private val view: DataSetDetailView = mock()
    private val repository: DataSetTableRepositoryImpl = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private lateinit var presenter: DataSetDetailPresenter
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val updateProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    @Before
    fun setUp() {
        presenter = DataSetDetailPresenter(
            view,
            repository,
            scheduler,
            matomoAnalyticsController,
            updateProcessor,
        )
    }

    @Test
    fun `Should init with call to setCatOptComboName`() {
        whenever(repository.getDataSetCatComboName()) doReturn Single.just("catOptionName")
        whenever(repository.dataSetInstance()) doReturn Flowable.just(defaultDataSetInstance)
        whenever(repository.getPeriod()) doReturn Single.just(defaultPeriod)
        whenever(repository.isComplete()) doReturn Single.just(false)
        whenever(repository.getDataSet()) doReturn Single.just(defaultDataSet)
        whenever(view.observeReopenChanges()) doReturn Flowable.empty()

        presenter.init()

        verify(view).setCatOptComboName("catOptionName")
        verify(view).setDataSetDetails(defaultDataSetInstance, defaultPeriod, false)
        verify(view).setStyle(defaultStyle)
    }

    @Test
    fun `Should init with call to hideCatOptCombo`() {
        whenever(repository.getDataSetCatComboName()) doReturn Single.just("")
        whenever(repository.dataSetInstance()) doReturn Flowable.just(defaultDataSetInstance)
        whenever(repository.getPeriod()) doReturn Single.just(defaultPeriod)
        whenever(repository.isComplete()) doReturn Single.just(false)
        whenever(repository.getDataSet()) doReturn Single.just(defaultDataSet)
        whenever(view.observeReopenChanges()) doReturn Flowable.empty()

        presenter.init()

        verify(view).hideCatOptCombo()
        verify(view).setDataSetDetails(defaultDataSetInstance, defaultPeriod, false)
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
