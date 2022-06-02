package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import dagger.Module
import dagger.Provides
import io.reactivex.processors.FlowableProcessor
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.dataentry.DataEntryStore
import org.dhis2.data.forms.dataentry.SearchTEIRepository
import org.dhis2.data.forms.dataentry.SearchTEIRepositoryImpl
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.utils.reporting.CrashReportController
import org.hisp.dhis.android.core.D2

@Module
class DataValueModule(
    private val dataSetUid: String,
    private val sectionUid: String,
    private val orgUnitUid: String,
    private val periodId: String,
    private val attributeOptionComboUid: String,
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
        updateProcessor: FlowableProcessor<Unit>,
        featureConfigRepository: FeatureConfigRepository
    ): DataValuePresenter {
        return DataValuePresenter(
            view,
            repository,
            valueStore,
            schedulerProvider,
            updateProcessor,
            featureConfigRepository
        )
    }

    @Provides
    @PerFragment
    internal fun DataValueRepository(
        d2: D2,
        preferenceProvider: PreferenceProvider
    ): DataValueRepository {
        return DataValueRepository(
            d2,
            dataSetUid,
            sectionUid,
            orgUnitUid,
            periodId,
            attributeOptionComboUid,
            preferenceProvider
        )
    }

    @Provides
    @PerFragment
    internal fun searchRepository(d2: D2): SearchTEIRepository {
        return SearchTEIRepositoryImpl(d2, DhisEnrollmentUtils(d2))
    }

    @Provides
    @PerFragment
    fun valueStore(
        d2: D2,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils,
        searchRepository: SearchTEIRepository
    ): ValueStore {
        return ValueStoreImpl(
            d2,
            dataSetUid,
            DataEntryStore.EntryMode.DV,
            DhisEnrollmentUtils(d2),
            crashReportController,
            networkUtils,
            searchRepository
        )
    }
}
