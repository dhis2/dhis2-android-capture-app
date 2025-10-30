package org.dhis2.utils.granularsync

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.service.workManager.WorkManagerController
import org.hisp.dhis.android.core.D2

@Module
class GranularSyncModule(
    private val context: Context,
    private val view: GranularSyncContracts.View,
    private val syncContext: SyncContext,
) {
    @Provides
    fun providesViewModelFactory(
        d2: D2,
        schedulerProvider: SchedulerProvider,
        workManagerController: WorkManagerController,
        smsSyncProvider: SMSSyncProvider,
        repository: GranularSyncRepository,
    ): GranularSyncViewModelFactory =
        GranularSyncViewModelFactory(
            d2,
            view,
            repository,
            schedulerProvider,
            provideDispatchers(),
            syncContext,
            workManagerController,
            smsSyncProvider,
        )

    @Provides
    fun provideDispatchers() =
        object : DispatcherProvider {
            override fun io() = Dispatchers.IO

            override fun computation() = Dispatchers.Default

            override fun ui() = Dispatchers.Main
        }

    @Provides
    fun granularSyncRepository(
        d2: D2,
        dhisProgramUtils: DhisProgramUtils,
        periodUtils: DhisPeriodUtils,
        preferenceProvider: PreferenceProvider,
        resourceManager: ResourceManager,
    ): GranularSyncRepository =
        GranularSyncRepository(
            d2,
            syncContext,
            preferenceProvider,
            dhisProgramUtils,
            periodUtils,
            resourceManager,
            provideDispatchers(),
        )

    @Provides
    fun smsSyncProvider(
        d2: D2,
        colorUtils: ColorUtils,
    ): SMSSyncProvider =
        SMSSyncProviderImpl(
            d2,
            syncContext,
            ResourceManager(context, colorUtils),
        )
}
