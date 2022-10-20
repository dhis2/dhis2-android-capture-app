package org.dhis2.commons.sync

import androidx.fragment.app.FragmentActivity

interface SyncComponentProvider {
    fun showSyncStatusDialog(
        activity: FragmentActivity,
        recordUid: String,
        conflictType: ConflictType,
        orgUnitDataValue: String? = null,
        attributeComboDataValue: String? = null,
        periodIdDataValue: String? = null,
        dismissListener: OnDismissListener? = null
    )
}
