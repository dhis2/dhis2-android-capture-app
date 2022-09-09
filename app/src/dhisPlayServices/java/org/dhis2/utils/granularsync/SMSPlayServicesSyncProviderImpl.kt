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

    override val smsSender: SmsSubmitCase by lazy {
        d2.smsModule().smsSubmitCase()
    }

    override fun isPlayServicesEnabled() = true

    override fun getTaskOrNull(context: Context, senderNumber: String): Task<Void>? {
        return SmsRetriever.getClient(context).startSmsUserConsent(senderNumber)
    }
}
