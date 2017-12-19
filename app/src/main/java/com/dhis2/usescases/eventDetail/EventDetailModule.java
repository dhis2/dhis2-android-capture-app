package com.dhis2.usescases.eventDetail;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 19/12/2017.
 */

@Module
public class EventDetailModule {

    @Provides
    @PerActivity
    EventDetailContracts.View provideView(EventDetailActivity mobileActivity) {
        return mobileActivity;
    }

    @Provides
    @PerActivity
    EventDetailContracts.Presenter providePresenter(D2 d2, MetadataRepository metadataRepository) {
        return new EventDetailPresenter(d2, metadataRepository);
    }

}
