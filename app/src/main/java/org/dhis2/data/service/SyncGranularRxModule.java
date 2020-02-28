package org.dhis2.data.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;

import org.dhis2.data.dagger.PerService;
import org.dhis2.data.prefs.PreferenceProvider;
import org.hisp.dhis.android.core.D2;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

@Module
@PerService
public class SyncGranularRxModule {

    @Provides
    @PerService
    SyncPresenter syncPresenter(
            @NonNull Context context,
            @NonNull D2 d2,
            @NonNull PreferenceProvider preferences
    ) {
        WorkManager workManager = WorkManager.getInstance(context);

        return new SyncPresenterImpl(d2, preferences, workManager);
    }
}
