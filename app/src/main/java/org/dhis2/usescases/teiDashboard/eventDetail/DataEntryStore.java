package org.dhis2.usescases.teiDashboard.eventDetail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hisp.dhis.android.core.event.Event;

import java.util.Date;

import io.reactivex.Flowable;

/**
 * QUADRAM. Created by ppajuelo on 28/02/2018.
 */

public interface DataEntryStore {

    @NonNull
    Flowable<Long> save(@NonNull String uid, @Nullable String value);

    void updateEventStatus(Event eventModel);

    void skipEvent(Event eventModel);

    void rescheduleEvent(Event eventModel, Date newDate);

    void updateEvent(@NonNull Date eventDate, @NonNull Event eventModel);
}
