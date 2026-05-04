package org.dhis2.utils.granularsync.ui

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.SyncContext
import org.dhis2.utils.granularsync.SyncDate
import org.dhis2.utils.granularsync.domain.SyncStatus
import org.dhis2.utils.granularsync.domain.SyncStatusData

class SyncUiStateMapper(
    private val syncContext: SyncContext,
    private val resourceManager: ResourceManager,
) {
    fun toUiState(
        data: SyncStatusData,
        shouldDismissOnUpdate: Boolean = false,
    ): SyncUiState =
        SyncUiState(
            syncState = data.syncState,
            title = getTitleForState(data.syncState),
            lastSyncDate = data.lastSyncDate?.let { SyncDate(it) },
            message = getMessageForState(data.syncState, data.targetName),
            mainActionLabel = getMainActionLabel(data.syncState),
            secondaryActionLabel = getSecondaryActionLabel(data.syncState),
            content = data.content,
            shouldDismissOnUpdate = shouldDismissOnUpdate,
        )

    fun missingTargetUiState(recordUid: String): SyncUiState =
        SyncUiState(
            syncState = SyncStatus.ERROR,
            title = getTitleForState(SyncStatus.ERROR),
            lastSyncDate = null,
            message = resourceManager.getString(R.string.resource_not_found, recordUid),
            mainActionLabel = getMainActionLabel(SyncStatus.ERROR),
            secondaryActionLabel = getSecondaryActionLabel(SyncStatus.ERROR),
            content = emptyList(),
        )

    private fun getTitleForState(status: SyncStatus): String =
        when (status) {
            SyncStatus.NOT_SYNCED ->
                resourceManager.getString(R.string.sync_dialog_title_not_synced)

            SyncStatus.ERROR -> resourceManager.getString(R.string.sync_dialog_title_error)
            SyncStatus.RELATIONSHIP,
            SyncStatus.SYNCED,
            -> resourceManager.getString(R.string.sync_dialog_title_synced)

            SyncStatus.WARNING -> resourceManager.getString(R.string.sync_dialog_title_warning)
            SyncStatus.UPLOADING -> resourceManager.getString(R.string.sync_dialog_title_syncing)
            SyncStatus.SENT_VIA_SMS,
            SyncStatus.SYNCED_VIA_SMS,
            -> resourceManager.getString(R.string.sync_dialog_title_sms_syced)
        }

    private fun getMessageForState(
        status: SyncStatus,
        targetName: String,
    ): String? =
        when (status) {
            SyncStatus.NOT_SYNCED -> getNotSyncedMessage(targetName)
            SyncStatus.SYNCED -> getSyncedMessage(targetName)
            SyncStatus.SENT_VIA_SMS,
            SyncStatus.SYNCED_VIA_SMS,
            -> getSmsSyncedMessage()

            SyncStatus.ERROR,
            SyncStatus.WARNING,
            SyncStatus.RELATIONSHIP,
            SyncStatus.UPLOADING,
            -> null
        }

    private fun getNotSyncedMessage(targetName: String): String =
        when (syncContext.conflictType()) {
            ConflictType.ALL ->
                resourceManager.getString(R.string.sync_dialog_message_not_synced_all)

            ConflictType.DATA_SET,
            ConflictType.DATA_VALUES,
            ConflictType.PROGRAM,
            ->
                resourceManager.getString(
                    R.string.sync_dialog_message_not_synced_program,
                    targetName,
                )

            ConflictType.TEI, ConflictType.EVENT ->
                resourceManager.getString(
                    R.string.sync_dialog_message_not_synced_tei,
                    targetName,
                )
        }

    private fun getSyncedMessage(targetName: String): String =
        when (syncContext.conflictType()) {
            ConflictType.ALL -> resourceManager.getString(R.string.sync_dialog_message_synced_all)
            ConflictType.DATA_SET,
            ConflictType.DATA_VALUES,
            ConflictType.PROGRAM,
            ->
                resourceManager.getString(
                    R.string.sync_dialog_message_synced_program,
                    targetName,
                )

            ConflictType.TEI, ConflictType.EVENT ->
                resourceManager.getString(
                    R.string.sync_dialog_message_synced_tei,
                    targetName,
                )
        }

    private fun getSmsSyncedMessage(): String? =
        when (syncContext.conflictType()) {
            ConflictType.ALL,
            ConflictType.DATA_SET,
            ConflictType.PROGRAM,
            -> null

            ConflictType.DATA_VALUES,
            ConflictType.TEI,
            ConflictType.EVENT,
            -> resourceManager.getString(R.string.sync_dialog_message_sms_synced)
        }

    private fun getMainActionLabel(status: SyncStatus): String? =
        when (status) {
            SyncStatus.NOT_SYNCED,
            SyncStatus.SENT_VIA_SMS,
            SyncStatus.SYNCED_VIA_SMS,
            -> resourceManager.getString(R.string.sync_dialog_action_send)

            SyncStatus.ERROR,
            SyncStatus.SYNCED,
            SyncStatus.WARNING,
            -> resourceManager.getString(R.string.sync_dialog_action_refresh)

            SyncStatus.UPLOADING,
            SyncStatus.RELATIONSHIP,
            -> null
        }

    private fun getSecondaryActionLabel(status: SyncStatus): String? =
        when (status) {
            SyncStatus.UPLOADING,
            SyncStatus.RELATIONSHIP,
            -> null

            SyncStatus.NOT_SYNCED,
            SyncStatus.ERROR,
            SyncStatus.SYNCED,
            SyncStatus.SENT_VIA_SMS,
            SyncStatus.SYNCED_VIA_SMS,
            SyncStatus.WARNING,
            -> resourceManager.getString(R.string.sync_dialog_action_not_now)
        }
}