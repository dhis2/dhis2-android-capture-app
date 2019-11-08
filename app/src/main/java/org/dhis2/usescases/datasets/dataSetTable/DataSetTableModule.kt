package org.dhis2.usescases.datasets.dataSetTable

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepository
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepositoryImpl
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2

@PerActivity
@Module
class DataSetTableModule(
    private val view: DataSetTableView,
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val catOptCombo: String
) {

    @Provides
    @PerActivity
    fun providesPresenter(
        DataSetTableRepository: DataSetTableRepository,
        schedulerProvider: SchedulerProvider,
        analyticsHelper: AnalyticsHelper
    ): DataSetTablePresenter {
        return DataSetTablePresenter(
            view,
            DataSetTableRepository,
            schedulerProvider,
            analyticsHelper
        )
    }

    @Provides
    @PerActivity
    fun DataSetTableRepository(d2: D2): DataSetTableRepository {
        return DataSetTableRepositoryImpl(d2, dataSetUid, periodId, orgUnitUid, catOptCombo)
    }

    @Provides
    @PerActivity
    fun DataSetInitialRepository(d2: D2): DataSetInitialRepository {
        return DataSetInitialRepositoryImpl(d2, dataSetUid)
    }
}
