package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.forms.dataentry.DataEntryStore
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.prefs.PreferenceProvider
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
        analyticsHelper: AnalyticsHelper,
        preferenceProvider: PreferenceProvider
    ): DataValuePresenter {
        return DataValuePresenter(
            view,
            repository,
            valueStore,
            schedulerProvider,
            analyticsHelper,
            preferenceProvider,
            dataSetUid
        )
    }

    @Provides
    @PerFragment
    internal fun DataValueRepository(d2: D2): DataValueRepository {
        return DataValueRepositoryImpl(d2, dataSetUid)
    }

    @Provides
    @PerFragment
    fun valueStore(d2: D2): ValueStore {
        return ValueStoreImpl(d2, dataSetUid, DataEntryStore.EntryMode.DV)
    }
}
