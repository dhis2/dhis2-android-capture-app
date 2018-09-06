package org.dhis2.usescases.main.program;

import android.support.annotation.NonNull;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.Period;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

interface HomeRepository {

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<Pair<Integer, String>> numberOfRecords(ProgramModel program);

    @NonNull
    Flowable<List<ProgramViewModel>> programModels(List<Date> dates, Period period, String orgUnitsId);

}