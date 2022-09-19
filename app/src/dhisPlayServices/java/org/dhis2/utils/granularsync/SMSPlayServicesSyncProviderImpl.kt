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
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase

class SMSPlayServicesSyncProviderImpl(
    override val d2: D2,
    override val conflictType: SyncStatusDialog.ConflictType,
    override val recordUid: String,
    override val dvOrgUnit: String?,
    override val dvAttrCombo: String?,
    override val dvPeriodId: String?,
    override val resourceManager: ResourceManager
) : SMSSyncProvider {

    private val smsReceiver: BroadcastReceiver = object : SmsResponseReceiver() {
        override fun onConsentIntentReceived(intent: Intent) {
            smsRetrieverResultLauncher?.launch(intent)
            TODO("Not yet implemented")
        }
    }

    override val smsSender: SmsSubmitCase by lazy {
        d2.smsModule().smsSubmitCase()
    }

    override fun isPlayServicesEnabled() = true

    override fun getTaskOrNull(context: Context, senderNumber: String): Task<Void>? {
        return SmsRetriever.getClient(context).startSmsUserConsent(senderNumber)
    }

    override fun getSSMSIntentFilter() = SmsRetriever.SMS_RETRIEVED_ACTION

    override fun getSendPermission() = SmsRetriever.SEND_PERMISSION

    override fun registerSMSReceiver(context: Context, sendPermission: String?){
        context.registerReceiver(
            smsReceiver,
            IntentFilter(presenter.getSmsProvider().getSSMSIntentFilter()),
            sendPermission,
            null
        )
    }

    fun unregisterSMSReceiver(context: Context){
        context?.unregisterReceiver(smsReceiver)
    }

    override fun convertSimpleEvent(): Single<ConvertTaskResult> {
        return smsSender.compressSimpleEvent(recordUid).map { msg -> ConvertTaskResult.Message(msg) }
    }

    override fun convertTrackerEvent(): Single<ConvertTaskResult> {
        return smsSender.compressTrackerEvent(recordUid).map { msg -> ConvertTaskResult.Message(msg) }
    }

    override fun convertEnrollment(): Single<ConvertTaskResult> {
        return smsSender.compressEnrollment(recordUid).map { msg -> ConvertTaskResult.Message(msg) }
    }

    override fun convertDataSet(): Single<ConvertTaskResult> {
        return smsSender.compressDataSet(recordUid,dvOrgUnit,dvPeriodId,dvAttrCombo).map { msg -> ConvertTaskResult.Message(msg) }
    }
}
