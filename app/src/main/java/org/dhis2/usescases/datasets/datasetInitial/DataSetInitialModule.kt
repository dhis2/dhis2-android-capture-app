package org.dhis2.usescases.datasets.datasetInitial

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2

/**
 * QUADRAM. Created by ppajuelo on 24/09/2018.
 */
@PerActivity
@Module
class DataSetInitialModule internal constructor(
    private val view: DataSetInitialView,
    private val dataSetUid: String
) {

    @Provides
    @PerActivity
    internal fun provideView(activity: DataSetInitialActivity): DataSetInitialView {
        return activity
    }

    @Provides
    @PerActivity
    internal fun providesPresenter(
        dataSetInitialRepository: DataSetInitialRepository,
        schedulerProvider: SchedulerProvider
    ): DataSetInitialPresenter {
        return DataSetInitialPresenter(view, dataSetInitialRepository, schedulerProvider)
    }

    @Provides
    @PerActivity
    internal fun dataSetInitialRepository(d2: D2): DataSetInitialRepository {
        return DataSetInitialRepositoryImpl(d2, dataSetUid)
    }
}
