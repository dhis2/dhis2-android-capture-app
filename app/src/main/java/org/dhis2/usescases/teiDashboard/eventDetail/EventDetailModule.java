package org.dhis2.usescases.teiDashboard.eventDetail;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.user.UserRepository;
import org.hisp.dhis.android.core.D2;

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
    EventDetailContracts.Presenter providePresenter(EventDetailRepository eventDetailRepository, DataEntryStore dataEntryStore) {
        return new EventDetailPresenter(eventDetailRepository, dataEntryStore);
    }

    @Provides
    @PerActivity
    EventDetailRepository eventDetailRepository(BriteDatabase briteDatabase, D2 d2) {
        return new EventDetailRepositoryImpl(briteDatabase, eventUid, teiUid, d2);
    }

    @Provides
    @PerActivity
    DataEntryStore dataEntryRepository(@NonNull BriteDatabase briteDatabase, UserRepository userRepository) {
        return new DataValueStore(briteDatabase, userRepository, eventUid,teiUid);

    }
}
