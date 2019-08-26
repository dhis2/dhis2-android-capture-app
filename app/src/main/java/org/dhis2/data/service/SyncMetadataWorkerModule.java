package org.dhis2.data.service;

import org.dhis2.data.dagger.PerService;
import org.dhis2.data.sharedPreferences.SharePreferencesProvider;
import org.hisp.dhis.android.core.D2;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 24/10/2018.
 */

@Module
@PerService
public class SyncMetadataWorkerModule {

    @Provides
    @PerService
    SyncPresenter getSyncPresenter(@Nonnull D2 d2, SharePreferencesProvider provider) {
        return new SyncPresenterImpl(d2, provider);
    }

}
