package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.FormSectionViewModel;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface EventSummaryRepository {

    @NonNull
    Flowable<List<FormSectionViewModel>> programStageSections(String eventUid);
}
