/*
 * Copyright (c) 2004 - 2019, University of Oslo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.utils.granularsync

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
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
    ): GranularSyncViewModelFactory {
        return GranularSyncViewModelFactory(
            d2,
            view,
            repository,
            schedulerProvider,
            object : DispatcherProvider {
                override fun io() = Dispatchers.IO

                override fun computation() = Dispatchers.Default

                override fun ui() = Dispatchers.Main
            },
            syncContext,
            workManagerController,
            smsSyncProvider,
        )
    }

    @Provides
    fun granularSyncRepository(
        d2: D2,
        dhisProgramUtils: DhisProgramUtils,
        periodUtils: DhisPeriodUtils,
        preferenceProvider: PreferenceProvider,
        resourceManager: ResourceManager,
    ): GranularSyncRepository = GranularSyncRepository(
        d2,
        syncContext,
        preferenceProvider,
        dhisProgramUtils,
        periodUtils,
        resourceManager,
    )

    @Provides
    fun smsSyncProvider(d2: D2, colorUtils: ColorUtils): SMSSyncProvider {
        return SMSPlayServicesSyncProviderImpl(
            d2,
            syncContext,
            ResourceManager(context, colorUtils),
        )
    }
}
