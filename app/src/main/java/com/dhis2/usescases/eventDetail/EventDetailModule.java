package com.dhis2.usescases.eventDetail;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 19/12/2017.
 *
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
    EventDetailContracts.Presenter providePresenter(EventDetailRepository eventDetailRepository, MetadataRepository metadataRepository) {
        return new EventDetailPresenter(eventDetailRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    EventDetailRepository eventDetailRepository(BriteDatabase briteDatabase) {
        return new EventDetailRepositoryImpl(briteDatabase);
    }
}
