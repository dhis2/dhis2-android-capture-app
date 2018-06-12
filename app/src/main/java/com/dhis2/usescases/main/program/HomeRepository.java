package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;

import com.dhis2.data.tuples.Pair;
import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

interface HomeRepository {

    @NonNull
    Observable<List<ProgramModel>> programs(String orgUnitsIdQuery);

    @NonNull
    Observable<List<ProgramModel>> programs(List<Date> dates, Period period);

    @NonNull
    Flowable<List<ProgramModel>> programs(List<Date> dates, Period period, String orgUnitsIdQuery);

    @NonNull
    Observable<List<EventModel>> eventModels(String programUid);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<Pair<Integer, String>> numberOfRecords(ProgramModel program);

    @NonNull
    Flowable<List<ProgramViewModel>> programModels();

}