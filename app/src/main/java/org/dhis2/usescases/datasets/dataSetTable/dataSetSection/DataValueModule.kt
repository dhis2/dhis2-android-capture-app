package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import com.squareup.sqlbrite2.BriteDatabase
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.forms.dataentry.DataEntryStore
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2

@Module
class DataValueModule(
    private val dataSetUid: String,
    private val view: DataValueContract.View
) {

    @Provides
    @PerFragment
    internal fun provideView(fragment: DataSetSectionFragment): DataValueContract.View {
        return fragment
    }

    @Provides
    @PerFragment
    internal fun providesPresenter(
        repository: DataValueRepository,
        valueStore: ValueStore,
        schedulerProvider: SchedulerProvider,
        analyticsHelper: AnalyticsHelper
    ): DataValuePresenter {
        return DataValuePresenter(
            view,
            repository,
            valueStore,
            schedulerProvider,
            analyticsHelper
        )
    }

    @Provides
    @PerFragment
    internal fun DataValueRepository(d2: D2, briteDatabase: BriteDatabase): DataValueRepository {
        return DataValueRepositoryImpl(d2, briteDatabase, dataSetUid)
    }

    @Provides
    @PerFragment
    fun valueStore(d2: D2): ValueStore {
        return ValueStoreImpl(d2, dataSetUid, DataEntryStore.EntryMode.DV)
    }
}
