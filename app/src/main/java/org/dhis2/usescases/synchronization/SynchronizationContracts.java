package org.dhis2.usescases.synchronization;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import org.dhis2.data.service.SyncResult;
import org.dhis2.databinding.ActivitySynchronizationBinding;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.login.LoginActivity;

import io.reactivex.functions.Consumer;
import retrofit2.Response;

public class SynchronizationContracts {

    public interface View extends AbstractActivityContracts.View{

        ActivitySynchronizationBinding getBinding();

        void saveTheme(Integer themeId);

        void saveFlag(String s);
    }

    public interface Presenter{

        void init(View view);

        void syncMeta(int seconds, String scheduleTag);

        void syncReservedValues();

        void syncData(int seconds, String scheduleTag);

        void getTheme();
    }
}
