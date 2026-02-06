package org.dhis2.mobile.sync.di

import androidx.work.WorkManager
import org.dhis2.mobile.sync.data.AndroidSyncBackgroundJobAction
import org.dhis2.mobile.sync.data.AndroidSyncRepository
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.domain.SyncMetadata
import org.dhis2.mobile.sync.domain.SyncSettings
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

actual val syncModule =
    module {
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

        factoryOf(::SyncMetadata)

        factoryOf(::SyncSettings)
    }
