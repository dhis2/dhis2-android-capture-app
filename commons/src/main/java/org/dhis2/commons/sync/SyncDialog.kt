package org.dhis2.commons.sync

import androidx.fragment.app.FragmentActivity
import org.dhis2.commons.components.ComponentProvider

class SyncDialog(
    val activity: FragmentActivity,
    val recordUid: String,
    val conflictType: ConflictType,
    private val orgUnitDataValue: String? = null,
    private val attributeComboDataValue: String? = null,
    private val periodIdDataValue: String? = null,
    private val dismissListener: OnDismissListener? = null
) {
    fun show() {
        (activity.applicationContext as? ComponentProvider)
            ?.syncComponentProvider
            ?.showSyncStatusDialog(
                activity,
                recordUid,
                conflictType,
                orgUnitDataValue,
                attributeComboDataValue,
                periodIdDataValue,
                dismissListener
            )
    }
}
