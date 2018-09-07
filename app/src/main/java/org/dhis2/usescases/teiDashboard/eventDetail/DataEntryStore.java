package org.dhis2.usescases.teiDashboard.eventDetail;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.Date;

import io.reactivex.Flowable;

/**
 * QUADRAM. Created by ppajuelo on 28/02/2018.
 */

public interface DataEntryStore {

    @NonNull
    Flowable<Long> save(@NonNull String uid, @Nullable String value);

    void updateEventStatus(EventModel eventModel);

    void updateEvent(@NonNull Date eventDate, @NonNull EventModel eventModel);
}
