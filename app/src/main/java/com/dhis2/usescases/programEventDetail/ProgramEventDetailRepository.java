package com.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface ProgramEventDetailRepository {

    @NonNull
    Observable<List<EventModel>> filteredProgramEvents(String programUid, String fromDate, String toDate, CategoryOptionComboModel categoryOptionComboModel,String orgUnitQuery);

    @NonNull
    Observable<List<EventModel>> filteredProgramEvents(String programUid, List<Date> dates, Period period, CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<CategoryOptionComboModel>> catCombo(String programUid);

    @NonNull
    Observable<List<TrackedEntityDataValueModel>> eventDataValues(EventModel eventModel);

    Observable<List<String>> eventDataValuesNew(EventModel eventModel);

    Observable<Boolean> writePermission(String programId);
}
