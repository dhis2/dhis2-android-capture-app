package org.dhis2.usescases.settings.models

import org.dhis2.usescases.settings.SettingItem
import org.hisp.dhis.android.core.settings.LimitScope

internal sealed class SettingsUiAction {
    data class OnItemClick(
        val settingItem: SettingItem,
    ) : SettingsUiAction()

    object SyncData : SettingsUiAction()

    data class OnSyncDataPeriodChanged(
        val periodInSeconds: Int,
    ) : SettingsUiAction()

    object SyncMetadata : SettingsUiAction()

    data class OnSyncMetaPeriodChanged(
        val periodInSeconds: Int,
    ) : SettingsUiAction()

    data class OnSaveLimitScope(
        val limitScope: LimitScope,
    ) : SettingsUiAction()

    data class OnSaveEventMaxCount(
        val count: Int,
    ) : SettingsUiAction()

    data class OnSaveTeiMaxCount(
        val count: Int,
    ) : SettingsUiAction()

    object OnSpecificProgramSettingsClick : SettingsUiAction()

    data class OnSaveReservedValuesToDownload(
        val count: Int,
    ) : SettingsUiAction()

    object OnManageReserveValues : SettingsUiAction()

    object OnOpenErrorLog : SettingsUiAction()

    object OnOpenTwoFASettings : SettingsUiAction()

    object OnDownload : SettingsUiAction()

    object OnShare : SettingsUiAction()

    object OnDeleteLocalData : SettingsUiAction()

    object OnCheckVersionUpdates : SettingsUiAction()

    data class SaveGateway(
        val gatewayNumber: String,
    ) : SettingsUiAction()

    data class SaveTimeout(
        val timeout: Int,
    ) : SettingsUiAction()

    data class EnableSMS(
        val gateWayNumber: String,
        val timeout: Int,
    ) : SettingsUiAction()

    object DisableSMS : SettingsUiAction()

    data class SaveResultSender(
        val resultSender: String,
    ) : SettingsUiAction()

    data class EnableWaitForResponse(
        val resultSender: String,
    ) : SettingsUiAction()

    object DisableWaitForResponse : SettingsUiAction()
}
