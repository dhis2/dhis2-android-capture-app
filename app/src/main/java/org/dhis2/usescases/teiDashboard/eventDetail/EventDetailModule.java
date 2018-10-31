package org.dhis2.usescases.teiDashboard.eventDetail;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.user.UserRepository;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 19/12/2017.
 */

@Module
public class EventDetailModule {

    private final String teiUid;
    String eventUid;

    EventDetailModule(String eventUid, String teiUid) {
        this.eventUid = eventUid;
        this.teiUid = teiUid;
    }

    @Provides
    @PerActivity
    EventDetailContracts.View provideView(EventDetailActivity mobileActivity) {
        return mobileActivity;
    }

    @Provides
    @PerActivity
    EventDetailContracts.Presenter providePresenter(EventDetailRepository eventDetailRepository, MetadataRepository metadataRepository, DataEntryStore dataEntryStore) {
        return new EventDetailPresenter(eventDetailRepository, metadataRepository, dataEntryStore);
    }

    @Provides
    @PerActivity
    EventDetailRepository eventDetailRepository(BriteDatabase briteDatabase) {
        return new EventDetailRepositoryImpl(briteDatabase, eventUid, teiUid);
    }

    @Provides
    @PerActivity
    DataEntryStore dataEntryRepository(@NonNull BriteDatabase briteDatabase, UserRepository userRepository) {
        return new DataValueStore(briteDatabase, userRepository, eventUid,teiUid);

    }
}
