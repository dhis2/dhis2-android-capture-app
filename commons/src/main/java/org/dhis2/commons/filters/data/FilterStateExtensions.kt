package org.dhis2.commons.filters.data

import androidx.annotation.StringRes
import org.dhis2.commons.R
import org.dhis2.commons.resources.ResourceManager
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
    SYNCED_VIA_SMS(R.string.sync_by_sms),
}

fun State.toStringResource(): Int {
    return when (this) {
        State.TO_POST -> StateFilter.TO_POST.stateName
        State.TO_UPDATE -> StateFilter.TO_UPDATE.stateName
        State.ERROR -> StateFilter.ERROR.stateName
        State.SYNCED -> StateFilter.SYNCED.stateName
        State.WARNING -> StateFilter.WARNING.stateName
        State.UPLOADING -> StateFilter.UPLOADING.stateName
        State.RELATIONSHIP -> StateFilter.RELATIONSHIP.stateName
        State.SENT_VIA_SMS -> StateFilter.SENT_VIA_SMS.stateName
        State.SYNCED_VIA_SMS -> StateFilter.SYNCED_VIA_SMS.stateName
    }
}

fun State.toStringValue(resourceManager: ResourceManager): String {
    return resourceManager.getString(toStringResource())
}
