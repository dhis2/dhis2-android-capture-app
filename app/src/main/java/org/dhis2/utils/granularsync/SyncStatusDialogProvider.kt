package org.dhis2.utils.granularsync

import androidx.fragment.app.FragmentActivity
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncComponentProvider

class SyncStatusDialogProvider : SyncComponentProvider {
    override fun showSyncStatusDialog(
        activity: FragmentActivity,
        recordUid: String,
        conflictType: ConflictType,
        orgUnitDataValue: String?,
        attributeComboDataValue: String?,
        periodIdDataValue: String?,
        dismissListener: OnDismissListener?
    ) {
        val syncBuilder = SyncStatusDialog.Builder()
            .setUid(recordUid)
            .setConflictType(conflictType)

        with(syncBuilder) {
            orgUnitDataValue?.let { setOrgUnit(it) }
            attributeComboDataValue?.let { setAttributeOptionCombo(it) }
            periodIdDataValue?.let { setPeriodId(it) }
            dismissListener?.let { onDismissListener(it) }
        }
        syncBuilder
            .build()
            .show(activity.supportFragmentManager, conflictType.name)
    }
}
