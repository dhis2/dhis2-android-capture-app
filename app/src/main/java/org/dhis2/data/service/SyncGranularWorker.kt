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

package org.dhis2.data.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Objects
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.utils.Constants.ATTRIBUTE_OPTION_COMBO
import org.dhis2.utils.Constants.CATEGORY_OPTION_COMBO
import org.dhis2.utils.Constants.CONFLICT_TYPE
import org.dhis2.utils.Constants.ORG_UNIT
import org.dhis2.utils.Constants.PERIOD_ID
import org.dhis2.utils.Constants.UID
import org.dhis2.utils.granularsync.SyncStatusDialog.ConflictType
import timber.log.Timber

class SyncGranularWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @Inject
    internal lateinit var presenter: SyncPresenter

    override fun doWork(): Result {
        Objects.requireNonNull((applicationContext as App).userComponent())!!
            .plus(SyncGranularRxModule()).inject(this)

        val uid = inputData.getString(UID)?.let { it } ?: return Result.failure()
        val conflictType = inputData.getString(CONFLICT_TYPE)?.let { ConflictType.valueOf(it) }

        try {
            presenter.uploadResources()
        } catch (e: Exception) {
            Timber.e(e)
        }

        return when (conflictType) {
            ConflictType.PROGRAM -> presenter.blockSyncGranularProgram(uid)
            ConflictType.TEI -> presenter.blockSyncGranularTei(uid)
            ConflictType.EVENT -> presenter.blockSyncGranularEvent(uid)
            ConflictType.DATA_SET -> presenter.blockSyncGranularDataSet(
                uid
            )
            ConflictType.DATA_VALUES ->
                presenter.blockSyncGranularDataValues(
                    uid,
                    inputData.getString(ORG_UNIT) as String,
                    inputData.getString(ATTRIBUTE_OPTION_COMBO) as String,
                    inputData.getString(PERIOD_ID) as String,
                    inputData.getStringArray(CATEGORY_OPTION_COMBO) as Array<String>
                )
            else -> Result.failure()
        }
    }
}
