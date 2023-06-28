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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import io.reactivex.Single
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.SyncContext
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import timber.log.Timber

class SMSPlayServicesSyncProviderImpl(
    override val d2: D2,
    override val syncContext: SyncContext,
    override val resourceManager: ResourceManager
) : SMSSyncProvider {

    private val confirmationMessage = MutableLiveData<Boolean?>(null)

    override fun observeConfirmationNumber(): LiveData<Boolean?> = confirmationMessage

    private val smsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION != intent.action) return

            val extras = intent.extras
            val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
            if (smsRetrieverStatus?.statusCode == CommonStatusCodes.SUCCESS) {
                val consentIntent: Intent? = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT)
                confirmationMessage.postValue(true)
            } else {
                confirmationMessage.postValue(false)
            }
        }
    }

    override var smsSender: SmsSubmitCase = d2.smsModule().smsSubmitCase()

    override fun isPlayServicesEnabled() = true

    override fun waitForSMSResponse(
        context: Context,
        senderNumber: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        context.registerReceiver(
            smsReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            SmsRetriever.SEND_PERMISSION,
            null
        )
        SmsRetriever.getClient(context).startSmsUserConsent(senderNumber).addOnCompleteListener {
            if (it.isSuccessful) {
                // Expect broadcast receiver
                onSuccess()
            } else {
                onFailure()
                unregisterSMSReceiver(context)
            }
        }
    }

    override fun unregisterSMSReceiver(context: Context) {
        try {
            context?.unregisterReceiver(smsReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }
    }

    override fun convertSimpleEvent(): Single<ConvertTaskResult> {
        return smsSender.compressSimpleEvent(syncContext.recordUid())
            .map { msg -> ConvertTaskResult.Message(msg) }
    }

    override fun convertTrackerEvent(): Single<ConvertTaskResult> {
        return smsSender.compressTrackerEvent(syncContext.recordUid())
            .map { msg -> ConvertTaskResult.Message(msg) }
    }

    override fun convertEnrollment(): Single<ConvertTaskResult> {
        return smsSender.compressEnrollment(syncContext.recordUid()).map { msg ->
            ConvertTaskResult.Message(msg)
        }
    }

    override fun convertDataSet(): Single<ConvertTaskResult> {
        return with(syncContext as SyncContext.DataSetInstance) {
            smsSender.compressDataSet(dataSetUid, orgUnitUid, periodId, attributeOptionComboUid)
                .map { msg -> ConvertTaskResult.Message(msg) }
        }
    }

    override fun onSmsNotAccepted(): SmsSendingService.SendingStatus {
        val submissionId = smsSender.submissionId
        return SmsSendingService.SendingStatus(
            submissionId,
            SmsSendingService.State.COUNT_NOT_ACCEPTED,
            null,
            0,
            0
        )
    }
}
