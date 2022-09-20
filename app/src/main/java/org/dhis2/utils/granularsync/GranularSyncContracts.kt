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
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import org.dhis2.usescases.general.AbstractActivityContracts
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.TrackerImportConflict

class GranularSyncContracts {

    interface View {
        fun showTitle(displayName: String)
        fun closeDialog()
        fun setState(state: State, conflicts: MutableList<TrackerImportConflict>)
        fun prepareConflictAdapter(conflicts: MutableList<TrackerImportConflict>)
        fun setLastUpdated(result: SyncDate)
        fun showRefreshTitle()
        fun logWaitingForServerResponse()
        fun logSmsReachedServer()
        fun logSmsReachedServerError()
        fun logSmsSent()
        fun logSmsNotSent()
        fun checkSmsPermission(): Boolean
        fun logOpeningSmsApp()
        fun openSmsApp(message: String, smsToNumber: String)
        fun updateState(state: State)
    }

    interface Presenter : AbstractActivityContracts.Presenter {
        fun isSMSEnabled(showSms: Boolean): Boolean
        fun configure(view: View)
        fun initGranularSync(): LiveData<List<WorkInfo>>
        fun initSMSSync(): LiveData<List<SmsSendingService.SendingStatus>>
        fun sendSMS()
        fun syncErrors(): List<ErrorViewModel>
        fun trackedEntityTypeNameFromEnrollment(enrollmentUid: String): String?
        fun onSmsNotAccepted()
        fun onSmsNotManuallySent(context: Context)
        fun onSmsSyncClick(
            context: Context,
            callback: (LiveData<List<SmsSendingService.SendingStatus>>) -> Unit
        )

        fun onSmsManuallySent(context: Context, confirmationCallback: (LiveData<Boolean?>) -> Unit)
        fun onConfirmationMessageStateChanged(messageReceived: Boolean?)
    }

    interface OnDismissListener {
        fun onDismiss(hasChanged: Boolean)
    }
}
