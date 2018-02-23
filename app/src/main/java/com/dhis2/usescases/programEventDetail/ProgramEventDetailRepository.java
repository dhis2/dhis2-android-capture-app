package com.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface ProgramEventDetailRepository {

    @NonNull
    Observable<List<EventModel>> programEvents(String programUid);

    @NonNull
    Observable<List<EventModel>> programEvents(String programUid, String fromDate, String toDate);

    @NonNull
    Observable<List<EventModel>> programEvents(String programUid, List<Date> dates, Period period);
}
