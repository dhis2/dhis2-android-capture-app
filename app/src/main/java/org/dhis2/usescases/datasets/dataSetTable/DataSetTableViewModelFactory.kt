package org.dhis2.usescases.datasets.dataSetTable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.processors.FlowableProcessor
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract.View
import org.dhis2.utils.analytics.AnalyticsHelper

class DataSetTableViewModelFactory(
    private val view: View,
    private val dataSetTableRepository: DataSetTableRepositoryImpl,
    private val periodUtils: DhisPeriodUtils,
    private val analyticsHelper: AnalyticsHelper,
    private val updateProcessor: FlowableProcessor<Unit>,
    private val dispatcherProvider: DispatcherProvider,
    private val openErrorSectionOnInit: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DataSetTablePresenter(
            view,
            dataSetTableRepository,
            periodUtils,
            dispatcherProvider,
            analyticsHelper,
            updateProcessor,
            openErrorSectionOnInit,
        ) as T
    }
}
