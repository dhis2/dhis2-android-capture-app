package com.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface ProgramEventDetailRepository {

    @NonNull
    Observable<List<EventModel>> programEvents(String programUid);
}
