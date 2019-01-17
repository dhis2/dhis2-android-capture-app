package org.dhis2.usescases.syncManager;

import android.content.Context;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.metadata.MetadataRepository;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by frodriguez on 4/13/2018.
 */

@Module
public final class SyncManagerModule {

    private Context context;

    public SyncManagerModule(Context context) {
        this.context = context;
    }

    @Provides
    @PerFragment
    SyncManagerContracts.Presenter providePresenter(MetadataRepository metadataRepository, D2 d2) {
        return new SyncManagerPresenter(metadataRepository, d2);
    }
}
