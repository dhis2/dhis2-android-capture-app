package org.dhis2.usecases.datasets

import com.nhaarman.mockitokotlin2.mock
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValueContract
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValuePresenter
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValueRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.junit.Before

class DataValuePresenterTest {

    private lateinit var presenter: DataValuePresenter

    private val view: DataValueContract.View = mock()
    private val dataValueRepository: DataValueRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setup() {
        presenter = DataValuePresenter(view, dataValueRepository, schedulers, analyticsHelper)
    }
}
