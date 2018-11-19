package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */

@PerActivity
@Module
public class EventCaptureModule {


    private final String eventUid;
    private final String programUid;

    public EventCaptureModule(String eventUid, String programUid) {
        this.eventUid = eventUid;
        this.programUid = programUid;
    }

    @Provides
    @PerActivity
    EventCaptureContract.Presenter providePresenter(@NonNull EventCaptureContract.EventCaptureRepository eventCaptureRepository) {
        return new EventCapturePresenterImpl(eventCaptureRepository);
    }

    @Provides
    @PerActivity
    EventCaptureContract.EventCaptureRepository provideRepository(@NonNull BriteDatabase briteDatabase) {
        return new EventCaptureRepositoryImpl(briteDatabase, eventUid);
    }
}
