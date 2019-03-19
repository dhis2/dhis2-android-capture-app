package org.dhis2.usescases.main.program;

import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;

interface HomeRepository {

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits(String parentUid);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Flowable<List<ProgramViewModel>> programModels(List<Date> dates, Period period, String orgUnitsId, int orgUnitsSize);

}