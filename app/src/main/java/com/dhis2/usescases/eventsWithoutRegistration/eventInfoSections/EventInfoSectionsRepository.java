package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.FormSectionViewModel;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface EventInfoSectionsRepository {


    @NonNull
    Flowable<List<FormSectionViewModel>> sections(@NonNull String eventUid);

}
