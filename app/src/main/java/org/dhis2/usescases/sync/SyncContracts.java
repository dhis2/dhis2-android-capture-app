package org.dhis2.usescases.sync;

import org.dhis2.data.sharedPreferences.SharePreferencesProvider;
import org.dhis2.databinding.ActivitySynchronizationBinding;
import org.dhis2.usescases.general.AbstractActivityContracts;

public class SyncContracts {

    public interface View extends AbstractActivityContracts.View{

        void saveTheme(Integer themeId);

        void saveFlag(String s);

        void setPreferences(SharePreferencesProvider provider);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init(View view);

        void syncMeta(int seconds, String scheduleTag);

        void syncReservedValues();

        void syncData(int seconds, String scheduleTag);

        void getTheme();
    }
}
