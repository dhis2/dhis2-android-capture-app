package org.dhis2.mobile.sync.di

import androidx.work.WorkManager
import org.dhis2.mobile.sync.data.AndroidSyncBackgroundJobAction
import org.dhis2.mobile.sync.data.AndroidSyncRepository
import org.dhis2.mobile.sync.data.GranularSyncWorker
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncDataSetRepository
import org.dhis2.mobile.sync.data.SyncDataSetRepositoryImpl
import org.dhis2.mobile.sync.data.SyncDataValueRepository
import org.dhis2.mobile.sync.data.SyncDataValueRepositoryImpl
import org.dhis2.mobile.sync.data.SyncDataWorker
import org.dhis2.mobile.sync.data.SyncEventRepository
import org.dhis2.mobile.sync.data.SyncEventRepositoryImpl
import org.dhis2.mobile.sync.data.SyncMetadataWorker
import org.dhis2.mobile.sync.data.SyncProgramRepository
import org.dhis2.mobile.sync.data.SyncProgramRepositoryImpl
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.data.SyncSettingsWorker
import org.dhis2.mobile.sync.data.SyncTeiRepository
import org.dhis2.mobile.sync.data.SyncTeiRepositoryImpl
import org.dhis2.mobile.sync.domain.CheckPeriodicJobs
import org.dhis2.mobile.sync.domain.SyncData
import org.dhis2.mobile.sync.domain.SyncDataSet
import org.dhis2.mobile.sync.domain.SyncDataValue
import org.dhis2.mobile.sync.domain.SyncEvent
import org.dhis2.mobile.sync.domain.SyncMetadata
import org.dhis2.mobile.sync.domain.SyncProgram
import org.dhis2.mobile.sync.domain.SyncSettings
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.dhis2.mobile.sync.domain.SyncTei
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val syncModule =
    module {

        singleOf(::SyncStatusController)

        factory {
            WorkManager.getInstance(get())
        }
        factory<SyncBackgroundJobAction> {
            AndroidSyncBackgroundJobAction(
                workManager = get(),
            )
        }

        factory<SyncRepository> {
            AndroidSyncRepository(get(), get(), get(), get(), get())
        }

        factoryOf(::SyncDataSetRepositoryImpl) bind SyncDataSetRepository::class
        factoryOf(::SyncDataValueRepositoryImpl) bind SyncDataValueRepository::class
        factoryOf(::SyncEventRepositoryImpl) bind SyncEventRepository::class
        factoryOf(::SyncProgramRepositoryImpl) bind SyncProgramRepository::class
        factoryOf(::SyncTeiRepositoryImpl) bind SyncTeiRepository::class

        factoryOf(::SyncMetadata)

        factoryOf(::SyncData)

        factoryOf(::SyncSettings)

        factoryOf(::CheckPeriodicJobs)

        factoryOf(::SyncDataSet)
        factoryOf(::SyncDataValue)
        factoryOf(::SyncEvent)
        factoryOf(::SyncProgram)
        factoryOf(::SyncTei)

        workerOf(::SyncDataWorker)
        workerOf(::SyncMetadataWorker)
        workerOf(::SyncSettingsWorker)
        workerOf(::GranularSyncWorker)
    }
