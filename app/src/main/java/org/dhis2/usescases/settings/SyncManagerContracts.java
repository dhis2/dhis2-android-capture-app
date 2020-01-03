package org.dhis2.usescases.settings;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by lmartin on 21/03/2018.
 */

public class SyncManagerContracts {

    interface View extends AbstractActivityContracts.View {

        Consumer<Pair<Integer, Integer>> setSyncData();

        void showInvalidGatewayError();

        void hideGatewayError();

        void wipeDatabase();

        void deleteLocalData();

        void showTutorial();

        void showSyncErrors(List<TrackerImportConflict> data);

        void showLocalDataDeleted(boolean error);

        void showSmsSettings(boolean enabled, String number, boolean waitForResponse,
                             String responseSender, int waitingForResponseTimeout);

        void syncData();

        void syncMeta();

        void openItem(int settingsItem);

        void displaySMSRefreshingData();

        void displaySMSEnabled(boolean isChecked);

        void requestNoEmptySMSGateway();
    }

    public interface Presenter {

        void validateGateway(String gateway);

        void onItemClick(int itemPosition);

        void init(SyncManagerContracts.View view);

        void syncData(int seconds, String scheduleTag);

        void syncMeta(int seconds, String scheduleTag);

        void syncData();

        void syncMeta();

        void disponse();

        void resetSyncParameters();

        void onWipeData();

        void wipeDb();

        void onDeleteLocalData();

        void deleteLocalData();

        void onReservedValues();

        void checkSyncErrors();

        void checkData();

        void cancelPendingWork(String meta);

        boolean dataHasErrors();

        boolean dataHasWarnings();

        void smsNumberSet(String number);

        void smsSwitch(boolean isChecked);

        void smsResponseSenderSet(String number);

        void smsWaitForResponse(boolean waitForResponse);

        void smsWaitForResponseTimeout(int timeout);
    }
}
