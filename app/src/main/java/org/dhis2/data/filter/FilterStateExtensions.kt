package org.dhis2.data.filter

import androidx.annotation.StringRes
import org.dhis2.R
import org.hisp.dhis.android.core.common.State

enum class StateFilter(@StringRes val stateName: Int) {
    TO_POST(R.string.state_to_post),
    TO_UPDATE(R.string.state_to_update),
    ERROR(R.string.state_error),
    SYNCED(R.string.state_synced),
    WARNING(R.string.state_warning),
    UPLOADING(R.string.state_uploading),
    RELATIONSHIP(R.string.state_relationship),
    SENT_VIA_SMS(R.string.sent_by_sms),
    SYNCED_VIA_SMS(R.string.sync_by_sms)
}

fun State.toStringResource():Int {
    return when (this) {
        State.TO_POST -> StateFilter.TO_POST.stateName
        State.TO_UPDATE -> TODO()
        State.ERROR -> TODO()
        State.SYNCED -> TODO()
        State.WARNING -> TODO()
        State.UPLOADING -> TODO()
        State.RELATIONSHIP -> TODO()
        State.SENT_VIA_SMS -> TODO()
        State.SYNCED_VIA_SMS -> TODO()
    }
}