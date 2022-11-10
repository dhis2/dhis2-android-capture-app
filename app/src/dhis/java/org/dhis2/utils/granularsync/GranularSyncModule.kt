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
import org.dhis2.R
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.sync.ConflictType
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.hisp.dhis.android.core.D2

@Module
class GranularSyncModule(
    private val context: Context,
    private val conflictType: ConflictType,
    private val recordUid: String,
    private val dvOrgUnit: String?,
    private val dvAttrCombo: String?,
    private val dvPeriodId: String?
) {

    @Provides
    fun providesPresenter(
        context: Context,
        d2: D2,
        schedulerProvider: SchedulerProvider,
        workManagerController: WorkManagerController,
        preferenceProvider: PreferenceProvider,
        smsSyncProvider: SMSSyncProvider
    ): GranularSyncContracts.Presenter {
        return GranularSyncPresenterImpl(
            d2,
            DhisProgramUtils(d2),
            schedulerProvider,
            conflictType,
            recordUid,
            dvOrgUnit,
            dvAttrCombo,
            dvPeriodId,
            workManagerController,
            ErrorModelMapper(context.getString(R.string.fk_message)),
            preferenceProvider,
            smsSyncProvider
        )
    }

    @Provides
    fun smsSyncProvider(d2: D2): SMSSyncProvider {
        return SMSSyncProviderImpl(
            d2,
            conflictType,
            recordUid,
            dvOrgUnit,
            dvAttrCombo,
            dvPeriodId,
            ResourceManager(context)
        )
    }
}
