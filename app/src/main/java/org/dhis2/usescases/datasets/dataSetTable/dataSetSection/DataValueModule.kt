package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.dataentry.SearchTEIRepository
import org.dhis2.data.forms.dataentry.SearchTEIRepositoryImpl
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.hisp.dhis.android.core.D2

@Module
class DataValueModule(
    private val dataSetUid: String,
    private val sectionUid: String,
    private val orgUnitUid: String,
    private val periodId: String,
    private val attributeOptionComboUid: String,
    private val view: DataValueContract.View,
    private val activityContext: Context
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
        tableDimensionStore: TableDimensionStore,
        schedulerProvider: SchedulerProvider,
        tableDataToTableModelMapper: TableDataToTableModelMapper,
        dispatcherProvider: DispatcherProvider
    ): DataValuePresenter {
        return DataValuePresenter(
            view,
            repository,
            valueStore,
            tableDimensionStore,
            schedulerProvider,
            tableDataToTableModelMapper,
            dispatcherProvider
        )
    }

    @Provides
    @PerFragment
    internal fun DataValueRepository(d2: D2): DataValueRepository {
        return DataValueRepository(
            d2,
            dataSetUid,
            sectionUid,
            orgUnitUid,
            periodId,
            attributeOptionComboUid
        )
    }

    @Provides
    @PerFragment
    internal fun TableDimensionStore(d2: D2) = TableDimensionStore(
        d2,
        dataSetUid,
        sectionUid
    )

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
        searchRepository: SearchTEIRepository,
        resourceManager: ResourceManager
    ): ValueStore {
        return ValueStoreImpl(
            d2,
            dataSetUid,
            EntryMode.DV,
            DhisEnrollmentUtils(d2),
            crashReportController,
            networkUtils,
            searchRepository,
            FieldErrorMessageProvider(activityContext),
            resourceManager
        )
    }

    @Provides
    @PerFragment
    fun provideTableDataToTableModelMapper(
        resourceManager: ResourceManager,
        repository: DataValueRepository
    ): TableDataToTableModelMapper {
        return TableDataToTableModelMapper(MapFieldValueToUser(resourceManager, repository))
    }
}
