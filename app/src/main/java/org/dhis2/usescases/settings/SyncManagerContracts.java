package org.dhis2.usescases.settings;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.settings.models.DataSettingsViewModel;
import org.dhis2.usescases.settings.models.ErrorViewModel;
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel;
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel;
import org.dhis2.usescases.settings.models.SMSSettingsViewModel;
import org.dhis2.usescases.settings.models.SyncParametersViewModel;

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

        boolean isGatewayValid();

        boolean isResultTimeoutValid();

        void enabledSMSSwitchAndSender(SMSSettingsViewModel settingsViewModel);
    }
}
