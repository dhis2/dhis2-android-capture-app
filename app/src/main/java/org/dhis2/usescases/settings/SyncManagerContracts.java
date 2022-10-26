package org.dhis2.usescases.settings;

import androidx.annotation.Nullable;
import androidx.work.WorkInfo;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.settings.models.DataSettingsViewModel;
import org.dhis2.usescases.settings.models.ErrorViewModel;
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel;
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel;
import org.dhis2.usescases.settings.models.SMSSettingsViewModel;
import org.dhis2.usescases.settings.models.SyncParametersViewModel;
import org.hisp.dhis.android.core.settings.LimitScope;

import java.util.List;

/**
 * QUADRAM. Created by lmartin on 21/03/2018.
 */

public class SyncManagerContracts {

    interface View extends AbstractActivityContracts.View {

        void showInvalidGatewayError();

        void hideGatewayError();

        void deleteLocalData();

        void showTutorial();

        void showSyncErrors(List<ErrorViewModel> data);

        void showLocalDataDeleted(boolean error);

        void syncData();

        void syncMeta();

        void openItem(SettingItem settingsItem);

        void displaySMSRefreshingData();

        void displaySMSEnabled(boolean isChecked);

        void requestNoEmptySMSGateway();

        void setDataSettings(DataSettingsViewModel dataSettings);

        void setMetadataSettings(MetadataSettingsViewModel metadataSettings);

        void setParameterSettings(SyncParametersViewModel parameterSettings);

        void setSMSSettings(SMSSettingsViewModel smsSettingsViewModel);

        void setReservedValuesSettings(ReservedValueSettingsViewModel reservedValueSettingsViewModel);

        void displaySmsEnableError();

        void onMetadataSyncInProgress();

        void onMetadataFinished();

        void onDataSyncInProgress();

        void onDataFinished();
    }

    public interface Presenter {

        int getMetadataPeriodSetting();

        int getDataPeriodSetting();

        void validateGatewayObservable(String gateway);

        void onItemClick(SettingItem settingItem);

        void init();

        void syncData(int seconds, String scheduleTag);

        void syncMeta(int seconds, String scheduleTag);

        void syncData();

        void syncMeta();

        void dispose();

        void resetSyncParameters();

        void wipeDb();

        void onDeleteLocalData();

        void deleteLocalData();

        void onReservedValues();

        void checkSyncErrors();

        void checkData();

        void cancelPendingWork(String meta);

        boolean isGatewaySetAndValid(String gateway);

        void saveLimitScope(LimitScope global);

        void saveEventMaxCount(Integer eventCount);

        void saveTeiMaxCount(Integer teiCount);

        void saveReservedValues(Integer reservedValuesCount);

        void saveGatewayNumber(String gatewayNumber);

        void saveSmsResultSender(String smsResultSender);

        void saveSmsResponseTimeout(Integer smsResponseTimeout);

        void saveWaitForSmsResponse(boolean shouldWait);

        void enableSmsModule(boolean enableSms);

        void resetFilters();

        void onWorkStatusesUpdate(@Nullable WorkInfo.State workState, String workerTag);
    }
}
