package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;

import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

interface HomeRepository {

    @NonNull
    Observable<List<ProgramModel>> programs(String fromDate, String toDate);

    @NonNull
    Observable<List<ProgramModel>> programs(List<Date> dates, Period period);


    @NonNull
    Flowable<List<ProgramModel>> programs(List<Date> dates, Period period, String orgUnitsIdQuery);

    @NonNull
    Observable<List<EventModel>> eventModels(String programUid);
    @NonNull
    Observable<List<EventModel>> eventModels();
    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();


}